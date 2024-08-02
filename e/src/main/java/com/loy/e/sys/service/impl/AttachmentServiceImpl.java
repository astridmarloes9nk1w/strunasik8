package com.loy.e.sys.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.loy.e.core.annotation.ControllerLogExeTime;
import com.loy.e.core.conf.Settings;
import com.loy.e.core.data.SuccessResponse;
import com.loy.e.core.util.Assert;
import com.loy.e.core.util.FileNameUtil;
import com.loy.e.sys.domain.entity.AttachmentEntity;
import com.loy.e.sys.repository.AttachmentRepository;

/**
 * 
 * @author Loy Fu qq群 540553957
 * @since 1.7
 * @version 1.0.0
 * 
 */

@RestController(value="attachmentService")
@RequestMapping(value = "attachment",method={RequestMethod.POST,RequestMethod.GET})
@Transactional
public class AttachmentServiceImpl {
	protected final Log logger = LogFactory.getLog(AttachmentServiceImpl.class);
	@Autowired
	AttachmentRepository attachmentRepository;
	@Autowired
	Settings settings;
	
	public void save(MultipartFile multipartFile,String targetId){
		String fileName = multipartFile.getOriginalFilename();
		String fileNameSuffix = FileNameUtil.getFileSuffix(fileName);
		AttachmentEntity attachmentEntity = new AttachmentEntity();
		attachmentEntity.setTargetId(targetId);
		attachmentEntity.setFileShowName(fileName);
		attachmentRepository.save(attachmentEntity);
		String attachmentId = attachmentEntity.getId();
		fileName = attachmentId+fileNameSuffix;
		attachmentEntity.setFileName(fileName);
		try {
		    File file = new File(settings.getAttachmentBaseDirectory(),fileName);
			FileCopyUtils.copy(multipartFile.getBytes(), file);
		} catch (IOException e) {
			logger.error(e);
			Assert.throwException();
		}
	}
	
	@ControllerLogExeTime(description="删除附件")
	@RequestMapping(value="/del")
	public SuccessResponse  del(String id){
		attachmentRepository.delete(id);
		return SuccessResponse.newInstance();
	}
	
	@ControllerLogExeTime(description="下载附件",log=false)
	@RequestMapping(value="/download",method={RequestMethod.GET})
    public void  download(String id,HttpServletResponse response) throws IOException{
		AttachmentEntity attachmentEntity = attachmentRepository.get(id);
		String fileName = attachmentEntity.getFileName();
		response.setContentType("application/x-download");
		String fileDisplay = fileName;//下载文件时显示的文件保存名称  
		fileDisplay = URLEncoder.encode(fileDisplay,"UTF-8");  
		response.addHeader("Content-Disposition","attachment;filename=" + fileDisplay);  
		OutputStream out = response.getOutputStream();
		File file = new File(settings.getAttachmentBaseDirectory(),fileName);
		FileCopyUtils.copy(new FileInputStream(file), out);
		out.flush();
		out.close();
	}
}