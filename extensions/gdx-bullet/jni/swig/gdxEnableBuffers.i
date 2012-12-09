// PRE: Expects gdxBuffers.i to be included
// POST: Pointers to most of the basic types are mapped to Buffers

ENABLE_NIO_BUFFER_TYPEMAP(unsigned char, java.nio.ByteBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(short, java.nio.ShortBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(unsigned short, java.nio.IntBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(int, java.nio.IntBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(unsigned int, java.nio.LongBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(long, java.nio.IntBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(unsigned long, java.nio.LongBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(float, java.nio.FloatBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(double, java.nio.DoubleBuffer);
ENABLE_NIO_BUFFER_TYPEMAP(btScalar, java.nio.FloatBuffer);