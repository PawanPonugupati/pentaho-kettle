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


package org.pentaho.di.core.logging;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Note, this test must be run on separate JAVA instance, to be sure 
 * LoggingRegistry was not already initialized when using differed initialization
 * or do initialize immediate in static way.
 *
 */
@RunWith( MockitoJUnitRunner.class )
public class LoggingRegistrySingltonTest {

  /**
   * Test that LoggingRegistry is concurrent-sage initialized over multiple calls. Creating more than 1000 threads can
   * cause significant performance impact.
   * 
   * @throws InterruptedException
   * @throws ExecutionException
   * 
   */
  @Test( timeout = 30000 )
  public void testLoggingRegistryConcurrentInitialization() throws InterruptedException, ExecutionException {
    CountDownLatch start = new CountDownLatch( 1 );

    int count = 10;
    CompletionService<LoggingRegistry> drover = registerHounds( count, start );
    // fire!
    start.countDown();

    Set<LoggingRegistry> distinct = new HashSet<LoggingRegistry>();

    int i = 0;
    while ( i < count ) {
      Future<LoggingRegistry> complete = drover.poll( 15, TimeUnit.SECONDS );
      LoggingRegistry instance = complete.get();
      distinct.add( instance );
      i++;
    }
    Assert.assertEquals( "Only one singlton instance ;)", 1, distinct.size() );
  }

  CompletionService<LoggingRegistry> registerHounds( int count, CountDownLatch start ) {
    ExecutorService executor = Executors.newFixedThreadPool( count );
    CompletionService<LoggingRegistry> completionService = new ExecutorCompletionService<LoggingRegistry>( executor );
    for ( int i = 0; i < count; i++ ) {
      LogRegistryKicker hound = new LogRegistryKicker( start );
      completionService.submit( hound );
    }
    return completionService;
  }

  class LogRegistryKicker implements Callable<LoggingRegistry> {
    CountDownLatch start;

    LogRegistryKicker( CountDownLatch start ) {
      this.start = start;
    }

    @Override
    public LoggingRegistry call() throws Exception {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      return LoggingRegistry.getInstance();
    }
  }
}
