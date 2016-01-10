package com.kodcu.big.data;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.List;


/**
 * Created by Hakan on 1/10/2016.
 */
public class BookDAOImpl implements BookDAO {

    final DBCollection booksCollection;

    public BookDAOImpl(final DB catalog) {
        booksCollection = catalog.getCollection("catalog");
    }

    public List<DBObject> findAll(int i) {
        List<DBObject> posts;
        DBCursor cursor;
        if (i == -1 || i <= 0) {
            cursor = booksCollection.find();
        } else {
            cursor = booksCollection.find().limit(i);
        }
        try {
            posts = cursor.toArray();
        } finally {
            cursor.close();
        }
        return posts;
    }
}
