package com.kodcu.big.data;

import com.mongodb.*;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringEscapeUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

/**
 * Created by Hakan on 1/10/2016.
 */
public class AppController {
    private static final String IP_ADDRESS = System.getenv("OPENSHIFT_DIY_IP") != null ? System.getenv("OPENSHIFT_DIY_IP") : "localhost";
    private static final int PORT = System.getenv("OPENSHIFT_DIY_PORT") != null ? Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT")) : 8080;

    private final Configuration cfg;
    private final BookDAOImpl bookDAO;

    public static void main(String[] args) throws Exception {
        new AppController();
    }

    public AppController() throws Exception {
        final DB catalog = initializeMongoDB();
        this.bookDAO = new BookDAOImpl(catalog);
        this.cfg = createFreemarkerConfiguration();

        setIpAddress(IP_ADDRESS);
        setPort(PORT);
        initializeRoutes();
    }

    private DB initializeMongoDB() throws Exception {

        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        if (host == null) {
            MongoClient mongoClient = new MongoClient("localhost");
            return mongoClient.getDB("catalog");
        }
        int port = Integer.parseInt(System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
        String dbname = System.getenv("OPENSHIFT_APP_NAME");
        String username = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
        String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");

        MongoClient mongoClient = new MongoClient(new ServerAddress(host, port),
                Arrays.asList(MongoCredential.createCredential(username, dbname, password.toCharArray())));
        DB db = mongoClient.getDB(dbname);
        return db;
    }

    private Configuration createFreemarkerConfiguration() {
        Configuration retVal = new Configuration();
        retVal.setClassForTemplateLoading(AppController.class, "/pages");
        return retVal;
    }

    private void initializeRoutes() throws IOException {
        get(new FreemarkerBasedRoute("/", "books.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                List<DBObject> books = bookDAO.findAll(-1);
                SimpleHash root = new SimpleHash();
                root.put("books", books);
                template.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/limit/:l", "books.ftl") {
            @Override
            public void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String limit = StringEscapeUtils.escapeHtml4(request.params(":l"));
                List<DBObject> books = bookDAO.findAll(Integer.parseInt(limit));
                SimpleHash root = new SimpleHash();
                root.put("books", books);
                template.process(root, writer);
            }
        });

        get(new FreemarkerBasedRoute("/error", "error.ftl") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash root = new SimpleHash();
                root.put("error", "System has encountered an error.");
                template.process(root, writer);
            }
        });
    }

    abstract class FreemarkerBasedRoute extends Route {
        final Template template;

        /**
         * Constructor
         *
         * @param path The route path which is used for matching. (e.g. /hello, users/:name)
         */
        protected FreemarkerBasedRoute(final String path, final String templateName) throws IOException {
            super(path);
            template = cfg.getTemplate(templateName);
        }

        @Override
        public Object handle(Request request, Response response) {
            StringWriter writer = new StringWriter();
            try {
                doHandle(request, response, writer);
            } catch (Exception e) {
                e.printStackTrace();
                response.redirect("/error");
            }
            return writer;
        }

        protected abstract void doHandle(final Request request, final Response response, final Writer writer)
                throws IOException, TemplateException;

    }
}
