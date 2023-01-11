package com.example.filesread.controller;




import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.filesread.service.FileService;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;
    
 

//    @GetMapping("/readWordFile")
//    public String readWordFile(){
//        return this.fileService.readWordFile();
//    }
    
    @GetMapping("/readExcelFile")
    public String readExcelFile(@RequestParam("name") String name,@RequestParam("month") String month) throws IOException {
    	return this.fileService.readExcelFile(name,month);
    }
}
 
