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


package org.pentaho.di.ui.repository.model;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class RepositoriesModel extends XulEventSourceAdapter {

  private String username;
  private String password;
  private boolean showDialogAtStartup;
  private List<RepositoryMeta> availableRepositories;
  private RepositoryMeta selectedRepository;

  public RepositoriesModel() {
    super();
    availableRepositories = new ArrayList<RepositoryMeta>();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    String previousValue = this.username;
    this.username = username;
    this.firePropertyChange( "username", previousValue, username );
    checkIfModelValid();
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    String previousValue = this.password;
    this.password = password;
    this.firePropertyChange( "password", previousValue, password );
  }

  public boolean isShowDialogAtStartup() {
    return showDialogAtStartup;
  }

  public void setShowDialogAtStartup( boolean showDialogAtStartup ) {
    boolean previousValue = this.showDialogAtStartup;
    this.showDialogAtStartup = showDialogAtStartup;
    this.firePropertyChange( "showDialogAtStartup", previousValue, showDialogAtStartup );
  }

  public List<RepositoryMeta> getAvailableRepositories() {
    return availableRepositories;
  }

  public void setAvailableRepositories( List<RepositoryMeta> repositoryList ) {
    List<RepositoryMeta> previousValue = new ArrayList<RepositoryMeta>();
    previousValue.addAll( this.availableRepositories );
    this.availableRepositories = repositoryList;
    this.firePropertyChange( "availableRepositories", previousValue, repositoryList );
  }

  public void addToAvailableRepositories( RepositoryMeta meta ) {
    List<RepositoryMeta> previousValue = new ArrayList<RepositoryMeta>();
    previousValue.addAll( this.availableRepositories );
    this.availableRepositories.add( meta );
    this.firePropertyChange( "availableRepositories", previousValue, this.availableRepositories );
  }

  public void removeFromAvailableRepositories( RepositoryMeta meta ) {
    List<RepositoryMeta> previousValue = new ArrayList<RepositoryMeta>();
    previousValue.addAll( this.availableRepositories );
    this.availableRepositories.remove( meta );
    this.firePropertyChange( "availableRepositories", previousValue, this.availableRepositories );
  }

  public void clear() {
    setUsername( null );
    setPassword( null );
    setShowDialogAtStartup( true );
    setAvailableRepositories( null );
  }

  public void setSelectedRepositoryUsingName( String repositoryName ) {
    setSelectedRepository( getRepository( repositoryName ) );
  }

  public void setSelectedRepository( RepositoryMeta selectedRepository ) {
    RepositoryMeta previousValue = this.selectedRepository;
    this.selectedRepository = selectedRepository;
    this.firePropertyChange( "selectedRepository", previousValue, selectedRepository );
    checkIfModelValid();
  }

  public RepositoryMeta getSelectedRepository() {
    return selectedRepository;
  }

  public RepositoryMeta getRepository( String repositoryName ) {
    if ( availableRepositories != null && availableRepositories.size() > 0 ) {
      for ( RepositoryMeta meta : availableRepositories ) {
        if ( meta != null && meta.getName().equals( repositoryName ) ) {
          return meta;
        }
      }
    }
    return null;
  }

  public int getRepositoryIndex( RepositoryMeta repositoryMeta ) {
    int index = 0;
    if ( repositoryMeta != null && availableRepositories != null && availableRepositories.size() > 0 ) {
      for ( RepositoryMeta meta : availableRepositories ) {
        if ( meta != null && meta.getName().equals( repositoryMeta.getName() ) ) {
          break;
        } else {
          index++;
        }
      }
    } else {
      index = -1;
    }
    return index;
  }

  public RepositoryMeta getRepository( int index ) {
    return availableRepositories.get( index );
  }

  public void checkIfModelValid() {
    this.firePropertyChange( "valid", null, isValid() );
  }

  public boolean isValid() {
    return username != null && username.length() > 0 && selectedRepository != null;
  }
}
