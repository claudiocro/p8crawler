package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.QueryResultList;

public class DeleteOldFeedItemsServlet extends HttpServlet
{
  private static final Logger logger = Logger.getLogger(DeleteOldFeedItemsServlet.class.getName());

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    String source = req.getParameter("source");
    String cat = req.getParameter("cat");
    String timeType = req.getParameter("timeType");
    String timeValue = req.getParameter("timeValue");
    boolean delete = "1".equals(req.getParameter("delete"));
    
    boolean deleteImage = (req.getParameter("deleteImage") != null && "1".equals(req.getParameter("deleteImage")));
    boolean deleteImg2 = (req.getParameter("deleteImg2") != null && "1".equals(req.getParameter("deleteImg2")));
    
    logger.info("delete old items delete:"+delete+" deleteImage:"+deleteImage+" deleteImg2:"+deleteImg2);

    if ((source == null && cat == null) || (timeType == null) || ((!"M".equals(timeType)) && (!"D".equals(timeType))) || (timeValue == null) || (!Util.isInt(timeValue))) {
      logger.log(Level.SEVERE, "invalidParams");
      resp.getWriter().write("invalidParams");
      throw new IllegalArgumentException("invalidParams");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("FeedItem");
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    if ("M".equals(timeType)) {
      cal.add(2, Integer.valueOf(timeValue).intValue() * -1);
    } else if ("D".equals(timeType)) {
      cal.add(6, Integer.valueOf(timeValue).intValue() * -1);
    }
    cal.set(11, 0);
    cal.set(14, 0);
    cal.set(13, 0);
    cal.set(12, 0);
    List<Filter> filters = new ArrayList<Query.Filter>();
    filters.add(new Query.FilterPredicate("storeDate", Query.FilterOperator.LESS_THAN, cal.getTime()));
    
    if(source != null)
    	filters.add(new Query.FilterPredicate("source", Query.FilterOperator.EQUAL, source));
    else if(cat != null)
    	filters.add(new Query.FilterPredicate("categories", Query.FilterOperator.EQUAL, cat));
    
    query.setFilter(CompositeFilterOperator.and(filters));
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
    	if(delete)
    		Scheduler.scheduleDeleteItem(KeyFactory.keyToString(entity.getKey()), delete, deleteImage, deleteImg2);
    	else if(!delete && !Util.ITEM_STATUS_DELETED.equals(entity.getProperty("status")))
    		Scheduler.scheduleDeleteItem(KeyFactory.keyToString(entity.getKey()), delete, deleteImage, deleteImg2);
    	else {
    		logger.info("Skip mark deleted: "+entity.getKey().getName());
    	}
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error when delete: " + entity.getKey(), e);
      }
    }

    if (!resultList.isEmpty())
      Scheduler.scheduleDeleteOldFeedItems(resultList.getCursor().toWebSafeString(), source, cat, timeType, timeValue, delete, deleteImage, deleteImg2);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    doGet(req, resp);
  }
}