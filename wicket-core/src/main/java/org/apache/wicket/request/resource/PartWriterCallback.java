/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.request.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wicket.protocol.http.servlet.ResponseIOException;
import org.apache.wicket.request.resource.AbstractResource.WriteCallback;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.io.Streams;

/**
 * Used to read a part of an input stream and writes it to the output stream of the response taken
 * from attributes of the writeData method.
 * 
 * @author Tobias Soloschenko
 *
 */
public class PartWriterCallback extends WriteCallback
{
	private final InputStream inputStream;

	private final Long contentLength;

	private final Long startbyte;

	private Long endbyte;

	private int bufferSize;


	/**
	 * Creates a part writer callback.<br>
	 * <br>
	 * Reads a part of the given input stream. If the startbyte parameter is not null the number of
	 * bytes are skipped till the stream is read. If the endbyte is not null the stream is read till
	 * endbyte, else to the end of the whole stream. If startbyte and endbyte is null the whole
	 * stream is read.
	 * 
	 * @param inputStream
	 *            the input stream to be read
	 * @param the
	 *            content length
	 * @param startbyte
	 *            the start position to read from (if not null the number of bytes are skipped till
	 *            the stream is read)
	 * @param endbyte
	 *            the end position to read to (if not null the stream is going to be read till
	 *            endbyte, else to the end of the whole stream)
	 */
	public PartWriterCallback(InputStream inputStream, Long contentLength, Long startbyte,
		Long endbyte)
	{
		this.inputStream = inputStream;
		this.contentLength = contentLength;
		this.startbyte = startbyte;
		this.endbyte = endbyte;
	}

	/**
	 * Writes the data
	 * 
	 * @param attributes
	 *            the attributes to get the output stream of the response
	 * @throws IOException
	 *             if something went wrong while writing the data to the output stream
	 */
	@Override
	public void writeData(Attributes attributes) throws IOException
	{
		try
		{
			OutputStream outputStream = attributes.getResponse().getOutputStream();
			byte[] buffer = new byte[getBufferSize()];

			if (startbyte != null || endbyte != null)
			{
				// skipping the first bytes which are
				// requested to be skipped by the client
				if (startbyte != null)
				{
					inputStream.skip(startbyte);
				}

				// If there are no end bytes given read the whole stream till the end
				if (endbyte == null || Long.valueOf(-1).equals(endbyte))
				{
					endbyte = contentLength;
				}

				// The read bytes in the current buffer
				int readBytes;

				// The total bytes read
				long totalBytes = 0;

				while ((readBytes = inputStream.read(buffer)) != -1)
				{
					totalBytes += readBytes;

					// Check if the end byte is reached
					if (endbyte - totalBytes < 0)
					{
						// calculate the bytes left to be read in the current buffer
						// can be casted to int, because the the previous chunks are
						// subtracted - so it can't exceed buffer size
						int leftBytesToBeRead = (int)(totalBytes - startbyte) -
							(int)(totalBytes - endbyte);
						outputStream.write(buffer, 0, leftBytesToBeRead);
						break;
					}
					else
					{
						// If the end byte is not reached read the full buffer
						outputStream.write(buffer, 0, readBytes);
					}
				}
			}
			else
			{
				Streams.copy(inputStream, outputStream, getBufferSize());
			}
		}
		catch (ResponseIOException e)
		{
			// the client has closed the connection and
			// doesn't read the stream further on
			// (in tomcats
			// org.apache.catalina.connector.ClientAbortException)
			// we ignore this case
		}
	}

	/**
	 * Sets the buffer size used to send the data to the client
	 * 
	 * @return the buffer size used to send the data to the client (default is 4096)
	 */
	public int getBufferSize()
	{
		return bufferSize > 0 ? bufferSize : 4096;
	}

	/**
	 * Sets the buffer size used to send the data to the client
	 * 
	 * @param bufferSize
	 *            the buffer size used to send the data to the client
	 */
	public void setBufferSize(int bufferSize)
	{
		this.bufferSize = bufferSize;
	}

}
