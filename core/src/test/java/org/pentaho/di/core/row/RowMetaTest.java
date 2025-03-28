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


package org.pentaho.di.core.row;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class RowMetaTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  RowMetaInterface rowMeta = new RowMeta();
  ValueMetaInterface string;
  ValueMetaInterface integer;
  ValueMetaInterface date;

  ValueMetaInterface charly;
  ValueMetaInterface dup;
  ValueMetaInterface bin;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    string = ValueMetaFactory.createValueMeta( "string", ValueMetaInterface.TYPE_STRING );
    rowMeta.addValueMeta( string );
    integer = ValueMetaFactory.createValueMeta( "integer", ValueMetaInterface.TYPE_INTEGER );
    rowMeta.addValueMeta( integer );
    date = ValueMetaFactory.createValueMeta( "date", ValueMetaInterface.TYPE_DATE );
    rowMeta.addValueMeta( date );

    charly = ValueMetaFactory.createValueMeta( "charly", ValueMetaInterface.TYPE_SERIALIZABLE );

    dup = ValueMetaFactory.createValueMeta( "dup", ValueMetaInterface.TYPE_SERIALIZABLE );
    bin = ValueMetaFactory.createValueMeta( "bin", ValueMetaInterface.TYPE_BINARY );
  }

  private List<ValueMetaInterface> generateVList( String[] names, int[] types ) throws KettlePluginException {
    List<ValueMetaInterface> list = new ArrayList<>();
    for ( int i = 0; i < names.length; i++ ) {
      ValueMetaInterface vm = ValueMetaFactory.createValueMeta( names[ i ], types[ i ] );
      vm.setOrigin( "originStep" );
      list.add( vm );
    }
    return list;
  }

  @Test
  public void testRowMetaInitializingFromXmlNode() throws Exception {
    String testXmlNode;
    try ( InputStream in = RowMetaTest.class.getResourceAsStream( "rowMetaNode.xml" ) ) {
      testXmlNode = IOUtils.toString( in );
    }
    Document xmlDoc = XMLHandler.loadXMLString( testXmlNode );
    System.setProperty( Const.KETTLE_XML_EMPTY_TAG_YIELDS_EMPTY_VALUE, "N" );
    RowMeta rowMeta = spy( new RowMeta( xmlDoc.getFirstChild() ) );
    assertEquals( 2, rowMeta.getValueMetaList().size() );
    ValueMetaInterface valueMeta = rowMeta.getValueMeta( 0 );
    assertTrue( valueMeta instanceof ValueMetaDate );
    assertEquals( "testDate", valueMeta.getName() );
    assertNull( valueMeta.getConversionMask() );
    valueMeta = rowMeta.getValueMeta( 1 );
    assertTrue( valueMeta instanceof ValueMetaTimestamp );
    assertEquals( "testTimestamp", valueMeta.getName() );
    assertEquals( "yyyy/MM/dd HH:mm:ss.000000000", valueMeta.getConversionMask() );
  }

  @Test
  public void testGetValueMetaList() {
    List<ValueMetaInterface> list = rowMeta.getValueMetaList();
    assertTrue( list.contains( string ) );
    assertTrue( list.contains( integer ) );
    assertTrue( list.contains( date ) );
  }

  @Test
  public void testSetValueMetaList() throws KettlePluginException {
    List<ValueMetaInterface> setList = this.generateVList( new String[] { "alpha", "bravo" }, new int[] { 2, 2 } );
    rowMeta.setValueMetaList( setList );
    assertTrue( setList.contains( rowMeta.searchValueMeta( "alpha" ) ) );
    assertTrue( setList.contains( rowMeta.searchValueMeta( "bravo" ) ) );

    // check that it is avalable by index:
    assertEquals( 0, rowMeta.indexOfValue( "alpha" ) );
    assertEquals( 1, rowMeta.indexOfValue( "bravo" ) );
  }

  @Test
  public void testSetValueMetaListNullName() throws KettlePluginException {
    List<ValueMetaInterface> setList = this.generateVList( new String[] { "alpha", null }, new int[] { 2, 2 } );
    rowMeta.setValueMetaList( setList );
    assertTrue( setList.contains( rowMeta.searchValueMeta( "alpha" ) ) );
    assertFalse( setList.contains( rowMeta.searchValueMeta( null ) ) );

    // check that it is avalable by index:
    assertEquals( 0, rowMeta.indexOfValue( "alpha" ) );
    assertEquals( -1, rowMeta.indexOfValue( null ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testDeSynchronizationModifyingOriginalList() {
    // remember 0-based arrays
    int size = rowMeta.size();
    // should be added at the end
    rowMeta.getValueMetaList().add( charly );
    assertEquals( size, rowMeta.indexOfValue( "charly" ) );
  }

  @Test
  public void testExists() {
    assertTrue( rowMeta.exists( string ) );
    assertTrue( rowMeta.exists( date ) );
    assertTrue( rowMeta.exists( integer ) );
  }

  @Test
  public void testAddValueMetaValueMetaInterface() {
    rowMeta.addValueMeta( charly );
    assertTrue( rowMeta.getValueMetaList().contains( charly ) );
  }

  @Test
  public void testAddValueMetaNullName() {
    ValueMetaInterface vmi = new ValueMetaBase();
    rowMeta.addValueMeta( vmi );
    assertTrue( rowMeta.getValueMetaList().contains( vmi ) );
  }

  @Test
  public void testAddValueMetaIntValueMetaInterface() {
    rowMeta.addValueMeta( 1, charly );
    assertEquals( 1, rowMeta.getValueMetaList().indexOf( charly ) );
  }

  @Test
  public void testGetValueMeta() {
    // see before method insertion order.
    assertEquals( rowMeta.getValueMeta( 1 ), integer );
  }

  @Test
  public void testSetValueMeta() {
    rowMeta.setValueMeta( 1, charly );
    assertEquals( 1, rowMeta.getValueMetaList().indexOf( charly ) );
    assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    assertEquals( -1, rowMeta.indexOfValue( "integer" ) );
  }

  @Test
  public void testSetValueMetaDup() {
    rowMeta.setValueMeta( 1, dup );
    assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    assertEquals( -1, rowMeta.indexOfValue( "integer" ) );

    rowMeta.setValueMeta( 1, dup );
    assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    assertEquals( -1, rowMeta.indexOfValue( "integer" ) );

    rowMeta.setValueMeta( 2, dup );
    assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
    assertEquals( "Original is still the same (object)", 1, rowMeta.getValueMetaList().indexOf( dup ) );
    assertEquals( "Original is still the same (name)", 1, rowMeta.indexOfValue( "dup" ) );
    assertEquals( "Renaming happened", 2, rowMeta.indexOfValue( "dup_1" ) );
  }

  @Test
  public void testInsertValueMetaDup() {
    rowMeta.addValueMeta( 1, new ValueMetaInteger( integer.getName() ) );
    assertEquals( "inserted", 4, rowMeta.size() );
    assertEquals( "rename new", "integer_1", rowMeta.getValueMeta( 1 ).getName() );
    rowMeta.addValueMeta( new ValueMetaInteger( integer.getName() ) );
    assertEquals( "rename after", "integer_2", rowMeta.getValueMeta( 4 ).getName() );
  }

  @Test
  public void testRemoveValueMetaDup() throws Exception {
    rowMeta.removeValueMeta( date.getName() );
    assertEquals( "removed", 2, rowMeta.size() );
    rowMeta.addValueMeta( new ValueMetaInteger( integer.getName() ) );
    assertEquals( "rename after", "integer_1", rowMeta.getValueMeta( 2 ).getName() );
  }

  @Test
  public void testSetValueMetaNullName() {
    ValueMetaInterface vmi = new ValueMetaBase();
    rowMeta.setValueMeta( 1, vmi );
    assertEquals( 1, rowMeta.getValueMetaList().indexOf( vmi ) );
    assertEquals( "There is still 3 elements:", 3, rowMeta.size() );
  }

  @Test
  public void testIndexOfValue() {
    List<ValueMetaInterface> list = rowMeta.getValueMetaList();
    assertEquals( 0, list.indexOf( string ) );
    assertEquals( 1, list.indexOf( integer ) );
    assertEquals( 2, list.indexOf( date ) );
  }

  @Test
  public void testIndexOfNullValue() {
    assertEquals( -1, rowMeta.indexOfValue( null ) );
  }

  @Test
  public void testSearchValueMeta() {
    ValueMetaInterface vmi = rowMeta.searchValueMeta( "integer" );
    assertEquals( integer, vmi );
    vmi = rowMeta.searchValueMeta( "string" );
    assertEquals( string, vmi );
    vmi = rowMeta.searchValueMeta( "date" );
    assertEquals( date, vmi );
  }

  @Test
  public void testAddRowMeta() throws KettlePluginException {
    List<ValueMetaInterface> list =
      this.generateVList( new String[] { "alfa", "bravo", "charly", "delta" }, new int[] { 2, 2, 3, 4 } );
    RowMeta added = new RowMeta();
    added.setValueMetaList( list );
    rowMeta.addRowMeta( added );

    assertEquals( 7, rowMeta.getValueMetaList().size() );
    assertEquals( 5, rowMeta.indexOfValue( "charly" ) );
  }

  @Test
  public void testMergeRowMeta() throws KettlePluginException {
    List<ValueMetaInterface> list =
      this.generateVList( new String[] { "phobos", "demos", "mars" }, new int[] { 6, 6, 6 } );
    list.add( 1, integer );
    RowMeta toMerge = new RowMeta();
    toMerge.setValueMetaList( list );

    rowMeta.mergeRowMeta( toMerge );
    assertEquals( 7, rowMeta.size() );

    list = rowMeta.getValueMetaList();
    assertTrue( list.contains( integer ) );
    ValueMetaInterface found = null;
    for ( ValueMetaInterface vm : list ) {
      if ( vm.getName().equals( "integer_1" ) ) {
        found = vm;
        break;
      }
    }
    assertNotNull( found );
  }

  @Test
  public void testRemoveValueMetaString() throws KettleValueException {
    rowMeta.removeValueMeta( "string" );
    assertEquals( 2, rowMeta.size() );
    assertNotNull( rowMeta.searchValueMeta( "integer" ) );
    assertEquals( "integer", rowMeta.searchValueMeta( "integer" ).getName() );
    assertNull( rowMeta.searchValueMeta( "string" ) );
  }

  @Test
  public void testRemoveValueMetaInt() {
    rowMeta.removeValueMeta( 1 );
    assertEquals( 2, rowMeta.size() );
    assertNotNull( rowMeta.searchValueMeta( "date" ) );
    assertNotNull( rowMeta.searchValueMeta( "string" ) );
    assertNull( rowMeta.searchValueMeta( "notExists" ) );
    assertEquals( "date", rowMeta.searchValueMeta( "date" ).getName() );
    assertNull( rowMeta.searchValueMeta( "integer" ) );
  }

  @Test
  public void testLowerCaseNamesSearch() {
    assertNotNull( rowMeta.searchValueMeta( "Integer" ) );
    assertNotNull( rowMeta.searchValueMeta( "string".toUpperCase() ) );
  }

  @Test
  public void testMultipleSameNameInserts() {
    for ( int i = 0; i < 13; i++ ) {
      rowMeta.addValueMeta( integer );
    }
    String resultName = "integer_13";
    assertEquals( rowMeta.searchValueMeta( resultName ).getName(), resultName );
  }

  @Test
  public void testExternalValueMetaModification() {
    ValueMetaInterface vmi = rowMeta.searchValueMeta( "string" );
    vmi.setName( "string2" );
    assertNotNull( rowMeta.searchValueMeta( vmi.getName() ) );
  }

  @Test
  public void testSwapNames() throws KettlePluginException {
    ValueMetaInterface string2 = ValueMetaFactory.createValueMeta( "string2", ValueMetaInterface.TYPE_STRING );
    rowMeta.addValueMeta( string2 );
    assertSame( string, rowMeta.searchValueMeta( "string" ) );
    assertSame( string2, rowMeta.searchValueMeta( "string2" ) );
    string.setName( "string2" );
    string2.setName( "string" );
    assertSame( string2, rowMeta.searchValueMeta( "string" ) );
    assertSame( string, rowMeta.searchValueMeta( "string2" ) );
  }

  @Test
  public void testCopyRowMetaCacheConstructor() {
    Map<String, Integer> mapping = new HashMap<>();
    mapping.put( "a", 1 );
    RowMeta.RowMetaCache rowMetaCache = new RowMeta.RowMetaCache( mapping );
    RowMeta.RowMetaCache rowMetaCache2 = new RowMeta.RowMetaCache( rowMetaCache );
    assertEquals( rowMetaCache.mapping, rowMetaCache2.mapping );
    rowMetaCache = new RowMeta.RowMetaCache( mapping );
    rowMetaCache2 = new RowMeta.RowMetaCache( rowMetaCache );
    assertEquals( rowMetaCache.mapping, rowMetaCache2.mapping );
  }

  @Test
  public void testNeedRealClone() {
    RowMeta newRowMeta = new RowMeta();
    newRowMeta.addValueMeta( string );
    newRowMeta.addValueMeta( integer );
    newRowMeta.addValueMeta( date );
    newRowMeta.addValueMeta( charly );
    newRowMeta.addValueMeta( dup );
    newRowMeta.addValueMeta( bin );
    List<Integer> list = newRowMeta.getOrCreateValuesThatNeedRealClone( newRowMeta.valueMetaList );
    assertEquals( 3, list.size() ); // Should be charly, dup and bin
    assertTrue( list.contains( 3 ) ); // charly
    assertTrue( list.contains( 4 ) ); // dup
    assertTrue( list.contains( 5 ) ); // bin
    newRowMeta.addValueMeta( charly ); // should have nulled the newRowMeta.needRealClone
    assertNull( newRowMeta.needRealClone ); // null because of the new add
    list = newRowMeta.getOrCreateValuesThatNeedRealClone( newRowMeta.valueMetaList );
    assertNotNull( newRowMeta.needRealClone );
    assertEquals( 4, list.size() ); // Should still be charly, dup, bin, charly_1
    newRowMeta.addValueMeta( bin ); // add new binary, should null out needRealClone again
    assertNull( newRowMeta.needRealClone ); // null because of the new add
    list = newRowMeta.getOrCreateValuesThatNeedRealClone( newRowMeta.valueMetaList );
    assertNotNull( newRowMeta.needRealClone );
    assertEquals( 5, list.size() ); // Should be charly, dup and bin, charly_1, bin_1

    newRowMeta.addValueMeta( string ); // add new string, should null out needRealClone again
    assertNull( newRowMeta.needRealClone ); // null because of the new add
    list = newRowMeta.getOrCreateValuesThatNeedRealClone( newRowMeta.valueMetaList );
    assertNotNull( newRowMeta.needRealClone );
    assertEquals( 5, list
      .size() ); // Should still only be charly, dup and bin, charly_1, bin_1 - adding a string doesn't change of result
  }

  @Test
  public void testMergeRowMetaWithOriginStep() throws Exception {

    List<ValueMetaInterface> list =
      this.generateVList( new String[] { "phobos", "demos", "mars" }, new int[] { 6, 6, 6 } );
    list.add( 1, integer );
    RowMeta toMerge = new RowMeta();
    toMerge.setValueMetaList( list );

    rowMeta.mergeRowMeta( toMerge, "newOriginStep" );
    assertEquals( 7, rowMeta.size() );

    list = rowMeta.getValueMetaList();
    assertTrue( list.contains( integer ) );
    ValueMetaInterface found = null;
    ValueMetaInterface other = null;
    for ( ValueMetaInterface vm : list ) {
      if ( vm.getName().equals( "integer_1" ) ) {
        found = vm;
        break;
      } else {
        other = vm;
      }
    }
    assertNotNull( found );
    assertEquals( "newOriginStep", found.getOrigin() );
    assertNotNull( other );
    assertEquals( "originStep", other.getOrigin() );

  }

  @Test
  public void testGetFieldNames() {
    rowMeta.clear();
    fillRowMeta();
    String[] names = rowMeta.getFieldNames();
    assertEquals( 10, names.length );
    assertEquals( "sample", names[ 0 ] );
    for ( int i = 1; i < names.length; i++ ) {
      assertEquals( "", names[ i ] );
    }
  }

  @Test
  public void testHashCode() {
    rowMeta.clear();
    byte[] byteArray = new byte[] { 49, 50, 51 };
    Object[] objArray = new Object[] { byteArray };
    try {
      assertEquals( 78512, rowMeta.hashCode( objArray ) );
    } catch ( KettleValueException e ) {
      e.printStackTrace();
    }
  }

  private void fillRowMeta() {
    rowMeta.addValueMeta( 0, new ValueMetaString( "sample" ) );
    for ( int i = 1; i < 10; i++ ) {
      rowMeta.addValueMeta( i, new ValueMetaInteger( null ) );
    }
  }
}
