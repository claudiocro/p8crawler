package ch.plus8.hikr.gappserver;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		/*StringTemplateGroup group = new StringTemplateGroup("html", "WEB-INF/html");
		
		StringTemplate template = group.getInstanceOf("index");
		//template.setFormalArguments(args)
		
		resp.getWriter().write(template.toString());
		*/
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
