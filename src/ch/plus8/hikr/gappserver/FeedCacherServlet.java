package ch.plus8.hikr.gappserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FeedCacherServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(FeedCacherServlet.class.getName());
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		
		logger.log(Level.INFO, "feedCacher called.");
		logger.log(Level.INFO, "Soft feedCacher called.");
		if(req.getParameter("param") != null && req.getParameter("paramv") != null) {
			Scheduler.cleanCache(req.getParameter("param"), req.getParameter("paramv"));
		} else {
			Scheduler.scheduleFeedCacher("1".equals(req.getParameter("force")));
		}
		
		/*String source = req.getParameter("source").toString(); 
		int page = Integer.valueOf(req.getParameter("page"));
		
		try {
			req.getRequestDispatcher("/feed").forward(req, resp);
		} catch (ServletException e) {
			logger.log(Level.SEVERE, "Could not dispatch to: " ,e);
		}
			
		if(++page <= MAX_COUNT)
			Util.scheduleFeedCacher(source, page);
		*/
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
	
}
