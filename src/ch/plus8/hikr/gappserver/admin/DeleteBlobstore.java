package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

@SuppressWarnings("serial")
public class DeleteBlobstore extends HttpServlet {

	private int MAX_DEL = 1000;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(req.getParameter("blob") == null)
			deleteAll();
		else
			deleteSingle(req.getParameter("blob"));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	protected void deleteAll() {
		Iterator<BlobInfo> iterator = new BlobInfoFactory().queryBlobInfos();
		int i = 0;
		
		Queue delqueue = QueueFactory.getQueue("deleteItem-queue");
		
		while (i < MAX_DEL && iterator.hasNext()) {
			TaskOptions params = TaskOptions.Builder.withUrl("/p8admin/deleteBlobstore");
			params.param("blob", iterator.next().getBlobKey().getKeyString());
			delqueue.add(params);
			i++;
		}
		
		if (iterator.hasNext()) {
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(TaskOptions.Builder.withUrl("/p8admin/deleteBlobstore"));
		}
	}
	
	
	protected void deleteSingle(String key) {
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		blobstoreService.delete(new BlobKey(key));
	}
}
