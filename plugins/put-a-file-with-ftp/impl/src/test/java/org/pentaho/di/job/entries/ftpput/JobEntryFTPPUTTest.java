/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.ftpput;

import com.enterprisedt.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryFTPPUTTest {

  private JobEntryFTPPUT entry;
  private FTPClient ftpClient;




  @Before
  public void setUp() throws Exception {
    ftpClient = mock( FTPClient.class );

    entry = spy( new JobEntryFTPPUT() );
    doReturn( ftpClient ).when( entry ).createFtpClient();
  }

  @Test
  public void createFtpClient_SetsLocalhostByDefault() throws Exception {
    entry.setServerName( null );
    entry.createAndSetUpFtpClient();
    assertEquals( "localhost", getHostFromClient() );
  }

  @Test
  public void createFtpClient_DoesNotChangePortByDefault() throws Exception {
    entry.setServerPort( null );
    entry.createAndSetUpFtpClient();
    verify( ftpClient, never() ).setRemotePort( anyInt() );
  }

  @Test
  public void createFtpClient_UsesProxyIfSet() throws Exception {
    entry.setProxyHost( "localhost" );
    entry.setProxyPort( "123" );
    entry.createAndSetUpFtpClient();

    assertEquals( "localhost", getHostFromClient() );
    // we cannot distinguish values of proxy and target server, as both of them refers to localhost
    // that is why I put invocation counter here
    verify( ftpClient, times( 2 ) ).setRemoteAddr( InetAddress.getByName( "localhost" ) );
    verify( ftpClient ).setRemotePort( 123 );
  }

  @Test
  public void createFtpClient_UsesTimeoutIfSet() throws Exception {
    entry.setTimeout( 10 );
    entry.createAndSetUpFtpClient();
    verify( ftpClient ).setTimeout( 10 );
  }


  private String getHostFromClient() throws Exception {
    ArgumentCaptor<InetAddress> addrCaptor = ArgumentCaptor.forClass( InetAddress.class );
    verify( ftpClient, atLeastOnce() ).setRemoteAddr( addrCaptor.capture() );
    return addrCaptor.getValue().getHostName();
  }
}
