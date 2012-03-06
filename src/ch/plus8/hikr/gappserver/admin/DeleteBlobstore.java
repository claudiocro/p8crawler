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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

@SuppressWarnings("serial")
public class DeleteBlobstore extends HttpServlet {

	private int MAX_DEL = 50;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		deleteAll();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		deleteAll();
	}

	protected void deleteAll() {
		List<BlobInfo> blobsToDelete = new LinkedList<BlobInfo>();
		Iterator<BlobInfo> iterator = new BlobInfoFactory().queryBlobInfos();
		int i = 0;
		while (i < MAX_DEL && iterator.hasNext()) {
			blobsToDelete.add(iterator.next());
			i++;
		}
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		for (BlobInfo blobInfo : blobsToDelete)
			blobstoreService.delete(blobInfo.getBlobKey());

		if (iterator.hasNext()) {
			Queue queue = QueueFactory.getQueue("deleteItem-queue");
			queue.add(TaskOptions.Builder.withUrl("/p8admin/deleteBlobstore"));
			
		}
	}
}
