package com.kodcu.big.data;

import com.mongodb.DBObject;

import java.util.List;

/**
 * Created by Hakan on 1/10/2016.
 */
public interface BookDAO {

    public List<DBObject> findAll(int i);

}
