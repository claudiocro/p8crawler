package ch.plus8.hikr.gappserver.admin;

import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteOldFeedItemsServlet extends HttpServlet
{
  private static final Logger logger = Logger.getLogger(DeleteOldFeedItemsServlet.class.getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    String source = req.getParameter("source");
    String timeType = req.getParameter("timeType");
    String timeValue = req.getParameter("timeValue");

    if ((source == null) || (timeType == null) || ((!"M".equals(timeType)) && (!"D".equals(timeType))) || (timeValue == null) || (!Util.isInt(timeValue))) {
      logger.log(Level.SEVERE, "invalidParams");
      resp.getWriter().write("invalidParams");
      throw new IllegalArgumentException("invalidParams");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("FeedItem");
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    if ("M".equals(timeType))
      cal.add(2, Integer.valueOf(timeValue).intValue() * -1);
    else if ("D".equals(timeType)) {
      cal.add(6, Integer.valueOf(timeValue).intValue() * -1);
    }
    cal.set(11, 0);
    cal.set(14, 0);
    cal.set(13, 0);
    cal.set(12, 0);
    query.addFilter("publishedDate", Query.FilterOperator.LESS_THAN, cal.getTime());
    query.addFilter("source", Query.FilterOperator.EQUAL, source);

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(20);
    if (req.getParameter("cursor") != null) {
      try {
        fetchOptions.startCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
        logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
      } catch (IllegalArgumentException e) {
        logger.log(Level.SEVERE, "Could not validate cursor string", e);
        resp.getWriter().write("Could not validate cursor string");
        return;
      }
    }

    PreparedQuery prepare = datastore.prepare(query);

    QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);

    for (Entity entity : resultList) {
      try {
        Scheduler.scheduleDeleteItem(entity.getKey().getName(), true);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error when delete: " + entity.getKey(), e);
      }
    }

    if (!resultList.isEmpty())
      Scheduler.scheduleDeleteOldFeedItems(resultList.getCursor().toWebSafeString(), source, timeType, timeValue, true);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    doGet(req, resp);
  }
}