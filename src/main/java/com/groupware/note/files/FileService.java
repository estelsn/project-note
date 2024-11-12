package com.groupware.note.files;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.groupware.note.DataNotFoundException;
import com.groupware.note.user.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
	private final FileRepository fileRepository;
	
	//경로 고정설정
	@Value("${spring.servlet.multipart.location}")
	private String directory;
	//파일 확장자 return
	public String extendsFile(String originFileName) {
		int fileExtensionIndex = originFileName.lastIndexOf('.');
		return originFileName.substring(fileExtensionIndex + 1);
	}
	//jpg , jpeg , bmp , gif , png, webp 타입일 경우 true return
	public boolean validFileExtension(String fileExtension) {
		String[] extension = {"jpg","jpeg","bmp","gif","png", "webp", "JPG","JPEG","BMP","GIF", "PNG", "WEBP"};
		if(Arrays.stream(extension).anyMatch(value -> value.equals(fileExtension))) {
			return true;
		}
		return false;
	}
	public boolean validExcelFileExtension(String fileExtension) {
		String[] extension = {"xls","xlsx","xlsm","xla","xlam"};
		if(Arrays.stream(extension).anyMatch(value -> value.equals(fileExtension))) {
			return true;
		}
		return false;
	}
	//FilePath 설정 고정경로+저장이름+파일확장자 조합
	public String getFilePath(String originFileName , String storeFileName) {
		String fileType = extendsFile(originFileName);
		return directory+storeFileName+"."+fileType;
	}
	public String setFilePath(String originFileName , String storeFileName) {
		String fileType = extendsFile(originFileName);
		return directory+storeFileName+"."+fileType;
	}

	//upload 메소드 -> Files 으로 return
	public Files uploadFile(MultipartFile multipartFiles) {
		Files file = new Files();
		try {
			if(multipartFiles==null||multipartFiles.isEmpty()) {
				return null;
			}
			String originFileName = multipartFiles.getOriginalFilename();
			String storeFileName = UUID.randomUUID().toString();
			while(!this.fileRepository.findByStoreFileName(storeFileName).isEmpty()) {
				storeFileName = UUID.randomUUID().toString();
			}
			String filePath = setFilePath(originFileName , storeFileName);
			Files _file = new Files();
			_file.setOriginFileName(originFileName);
			_file.setStoreFileName(storeFileName);
			multipartFiles.transferTo(new File(filePath));
			file = this.fileRepository.save(_file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataNotFoundException("");
		}
		return file;
	}
	//업데이트
	public Files updateFile(Files _file , MultipartFile multipartFile) {
		Files file = findByFiles(_file.getFileId());
		if(multipartFile==null||multipartFile.isEmpty()) {
			return file;
		}
		if(fileExists(_file)) {
			File originFile = new File(getFilePath(_file.getOriginFileName(), _file.getStoreFileName()));
			originFile.delete();
		}
		try {
			String originFileName = multipartFile.getOriginalFilename();
			String storeFileName = UUID.randomUUID().toString();
			file.setOriginFileName(originFileName);
			file.setStoreFileName(storeFileName);
			multipartFile.transferTo(new File(setFilePath(originFileName, storeFileName)));
			this.fileRepository.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}
	//download 메소드 -> ResponseEntity 으로 return
	public ResponseEntity<Resource> downloadFile(Files file) {
		String filePath = getFilePath(file.getOriginFileName() , file.getStoreFileName());
		try {
			String decodePath = URLDecoder.decode(filePath, "UTF-8");
			Path fileLocation = Paths.get(decodePath).toAbsolutePath().normalize();
			Resource resource = new UrlResource(fileLocation.toUri());
			if(!resource.exists()) {
				throw new RuntimeException("FileNotFound"+filePath);
			}
			String fileName=URLEncoder.encode(file.getOriginFileName(), "UTF-8").replace("+", "%20");			
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=\"" + fileName + "\";")
					.body(resource);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	//사진 보이게 하는거
	public ResponseEntity<Resource> photoView(Files file) throws MalformedURLException{
		String photoPath = getFilePath(file.getOriginFileName() , file.getStoreFileName());
		UrlResource resource = new UrlResource("file:" + photoPath);
		return ResponseEntity
				.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);			
	}
	
	public Files findByFiles(Integer id) {
		Optional<Files> _file = this.fileRepository.findById(id);
		if(_file.isEmpty()) {
			throw new DataNotFoundException("");
		}
		return _file.get();
	}
	public void delete(Files _file) {
		String filePath = getFilePath(_file.getOriginFileName(), _file.getStoreFileName());
		File file = new File(filePath);
		if(!fileExists(_file)||file.delete()) {
			this.fileRepository.delete(_file);
		}else {
			return;
		}
	}
	public boolean fileExists(Files file) {
		if(file==null) {
			return false;
		}
		String filePath = getFilePath(file.getOriginFileName(), file.getStoreFileName());
		File _file = new File(filePath);
		if(_file.exists()) {
			return true;
		}
		return false;
	}
}
