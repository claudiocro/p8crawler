package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

@SuppressWarnings("serial")
public class DeleteItemServlet extends HttpServlet {
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {  
    	GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		
		boolean delete = (req.getParameter("delete") != null && "1".equals(req.getParameter("delete")));
		boolean deleteImage = (req.getParameter("deleteImage") != null && "1".equals(req.getParameter("deleteImage")));
	    boolean deleteImg2 = (req.getParameter("deleteImg2") != null && "1".equals(req.getParameter("deleteImg2")));
	    
		feedRepository.deleteByKey(req.getParameter("key"), delete, deleteImage, deleteImg2);
    }  
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	doGet(req, resp);
    }
    
}
