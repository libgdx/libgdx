package com.badlogic.gdx.sqlite.desktop;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sqlite.DatabaseCursor;

public class DesktopCursor implements DatabaseCursor {
	
	public ResultSet resultSet = null;

	@Override
	public byte[] getBlob (int columnIndex) {
		try {
			Blob blob = resultSet.getBlob(columnIndex + 1);
			return blob.getBytes(1, (int) blob.length());
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the blog", e);
		}
		return null;
	}

	@Override
	public double getDouble (int columnIndex) {
		try {
			return resultSet.getDouble(columnIndex + 1);
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the double", e);
		}
		return 0;
	}

	@Override
	public float getFloat (int columnIndex) {
		try {
			return resultSet.getFloat(columnIndex + 1);
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the float", e);
		}
		return 0;
	}

	@Override
	public int getInt (int columnIndex) {
		try {
			return resultSet.getInt(columnIndex + 1);
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the int", e);
		}
		return 0;
	}

	@Override
	public long getLong (int columnIndex) {
		try {
			return resultSet.getLong(columnIndex + 1);
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the long", e);
		}
		return 0;
	}

	@Override
	public short getShort (int columnIndex) {
		try {
			return resultSet.getShort(columnIndex + 1);
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the short", e);
		}
		return 0;
	}

	@Override
	public String getString (int columnIndex) {
		try {
			return resultSet.getString(columnIndex + 1);
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in getting the string", e);
		}
		return null;
	}

	@Override
	public boolean next () {
		try {
			return resultSet.next();
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in moving the cursor to next", e);
		}
		return false;
	}

	@Override
	public int getCount () {
		return getRowCount(resultSet);
	}

	@Override
	public void close () {
		try {
			resultSet.close();
		} catch (SQLException e) {
			Gdx.app.log("Database", "There was an error in closing the cursor", e);
		}		
	}
	
	private int getRowCount(ResultSet resultSet) {
	    if (resultSet == null) {
	        return 0;
	    }
	    try {
	        resultSet.last();
	        return resultSet.getRow();
	    } catch (SQLException e) {
	   	 Gdx.app.log("Database", "There was an error counting the number of results", e);
	    } finally {
	        try {
	            resultSet.beforeFirst();
	        } catch (SQLException e) {
	      	  Gdx.app.log("Database", "There was an error counting the number of results", e);
	        }
	    }
	    return 0;
	}

}
