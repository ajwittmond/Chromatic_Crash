package com.flickshot.util;

import java.nio.ByteBuffer;

public interface Bufferable {
	public void set(ByteBuffer bb);
	public void put(ByteBuffer bb);
}
