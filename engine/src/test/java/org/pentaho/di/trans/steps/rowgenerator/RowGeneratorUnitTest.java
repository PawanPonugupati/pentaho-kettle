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


package org.pentaho.di.trans.steps.rowgenerator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class RowGeneratorUnitTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private RowGenerator rowGenerator;

  @BeforeClass
  public static void initEnvironment() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws KettleException {
    // add variable to row generator step
    StepMetaInterface stepMetaInterface = spy( new RowGeneratorMeta() );
    ( (RowGeneratorMeta) stepMetaInterface ).setRowLimit( "${ROW_LIMIT}" );
    String[] strings = {};
    when( ( (RowGeneratorMeta) stepMetaInterface ).getFieldName() ).thenReturn( strings );

    StepMeta stepMeta = new StepMeta();
    stepMeta.setStepMetaInterface( stepMetaInterface );
    stepMeta.setName( "ROW_STEP_META" );
    StepDataInterface stepDataInterface = stepMeta.getStepMetaInterface().getStepData();

    // add variable to transformation variable space
    Map<String, String> map = new HashMap<String, String>();
    map.put( "ROW_LIMIT", "1440" );
    TransMeta transMeta = spy( new TransMeta() );
    transMeta.injectVariables( map );
    when( transMeta.findStep( anyString() ) ).thenReturn( stepMeta );

    Trans trans = spy( new Trans( transMeta, null ) );
    when( trans.getSocketRepository() ).thenReturn( null );
    when( trans.getLogChannelId() ).thenReturn( "ROW_LIMIT" );

    //prepare row generator, substitutes variable by value from transformation variable space
    rowGenerator = spy( new RowGenerator( stepMeta, stepDataInterface, 0, transMeta, trans ) );
    rowGenerator.initializeVariablesFrom( trans );
    rowGenerator.init( stepMetaInterface, stepDataInterface );
  }

  @Test
  public void testReadRowLimitAsTransformationVar() throws KettleException {
    long rowLimit = ( (RowGeneratorData) rowGenerator.getStepDataInterface() ).rowLimit;
    assertEquals( rowLimit,  1440 );
  }

  @Test
  public void doesNotWriteRowOnTimeWhenStopped() throws KettleException, InterruptedException {
    TransMeta transMeta = new TransMeta( DefaultBowl.getInstance(),
      getClass().getResource( "safe-stop.ktr" ).getPath() );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( new String[] {} );
    trans.getSteps().get( 1 ).step.addRowListener( new RowAdapter() {
      @Override public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        trans.safeStop();
      }
    } );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 1, trans.getSteps().get( 0 ).step.getLinesWritten() );
    assertEquals( 1, trans.getSteps().get( 1 ).step.getLinesRead() );
  }
}
