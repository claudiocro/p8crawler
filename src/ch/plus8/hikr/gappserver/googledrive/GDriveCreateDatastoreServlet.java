package ch.plus8.hikr.gappserver.googledrive;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.admin.P8CrawlerGoogleServlet;

@SuppressWarnings("serial")
public class GDriveCreateDatastoreServlet extends P8CrawlerGoogleServlet {
	
	
	public static final String GDRIVEUSER_KIND = "gdrive:user";

	@Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			response.getWriter().print("<script type=\"text/javascript\">window.opener.App.datastoresController.load();window.close();</script>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }

	
	@Override
	protected String processOAuthRedirect(HttpServletRequest request) {
		return "/gdrive/createDatastore?foo=foo";
	}
	

	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}





}