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

package org.pentaho.di.repository.pur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

/**
 * @author Andrey Khayrutdinov
 */
public class PurRepository_DatabaseNames_IT extends PurRepositoryTestBase {

  public PurRepository_DatabaseNames_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Test
  public void saveDatabaseModifiedDate() throws Exception {
    Long testStart = System.currentTimeMillis();
    final String name = UUID.randomUUID().toString();
    DatabaseMeta db = saveDatabase( name );

    RepositoryObject info =
      purRepository.getObjectInformation( db.getObjectId(), RepositoryObjectType.DATABASE );
    assertNotNull( info.getModifiedDate() );
    assertTrue( testStart <= info.getModifiedDate().getTime() );
  }

  @Test
  public void getDatabaseId_ExactMatch() throws Exception {
    final String name = UUID.randomUUID().toString();
    DatabaseMeta db = saveDatabase( name );

    ObjectId id = purRepository.getDatabaseID( name );
    assertEquals( db.getObjectId(), id );
  }

  @Test
  public void getDatabaseId_InsensitiveMatch() throws Exception {
    final String name = "databaseWithCamelCase";
    final String lookupName = name.toLowerCase();
    assertNotSame( lookupName, name );

    DatabaseMeta db = saveDatabase( name );

    ObjectId id = purRepository.getDatabaseID( lookupName );
    assertEquals( db.getObjectId(), id );
  }

  @Test
  public void getDatabaseId_ReturnsExactMatch_PriorToCaseInsensitiveMatch() throws Exception {
    final String exact = "databaseExactMatch";
    final String similar = exact.toLowerCase();
    assertNotSame( similar, exact );

    DatabaseMeta db = saveDatabase( exact );

    // simulate legacy repository - store a DB with a name different only in case
    // it became illegal to store such DB via API, thus create the file accessing UnifiedRepository directly
    DatabaseMeta another = new DatabaseMeta();
    another.setName( similar );

    final String filename =
        PurRepository.checkAndSanitize( RepositoryFilenameUtils.escape( similar, unifiedRepository.getReservedChars() )
            + RepositoryObjectType.DATABASE.getExtension() );
    RepositoryFile file = new RepositoryFile.Builder( filename ).title( similar ).build();

    file =
        unifiedRepository.createFile( purRepository.getDatabaseMetaParentFolderId(), file, new NodeRepositoryFileData(
            new DatabaseDelegate( purRepository ).elementToDataNode( another ) ), null );

    assertNotNull( file.getId() );
    assertNotSame( file.getId().toString(), db.getObjectId().toString() );

    ObjectId id = purRepository.getDatabaseID( exact );
    assertEquals( db.getObjectId(), id );
  }

  private DatabaseMeta saveDatabase( String name ) throws Exception {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    purRepository.save( db, null, null );
    assertNotNull( db.getObjectId() );
    return db;
  }
}
