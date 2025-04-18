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


package org.pentaho.di.imp.rules;


import org.junit.Test;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportValidationResultType;
import org.pentaho.di.job.JobMeta;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class JobHasDescriptionImportRuleTest {

  @Test
  public void testGetSetMinLength() {
    JobHasDescriptionImportRule importRule = new JobHasDescriptionImportRule();

    // Check that, by default, some value is set
    assertTrue( 0 < importRule.getMinLength() );

    importRule.setMinLength( 10 );
    assertEquals( 10, importRule.getMinLength() );

    importRule.setMinLength( 25 );
    assertEquals( 25, importRule.getMinLength() );

    importRule.setMinLength( 0 );
    assertEquals( 0, importRule.getMinLength() );
  }

  @Test
  public void testVerifyRule_NullParameter_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_NotJobMetaParameter_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( "" );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_NullDescription_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription( null );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( jobMeta );

    assertNotNull( feedbackList );
    assertFalse( feedbackList.isEmpty() );
    ImportValidationFeedback feedback = feedbackList.get( 0 );
    assertNotNull( feedback );
    assertEquals( ImportValidationResultType.ERROR, feedback.getResultType() );
    assertTrue( feedback.isError() );
  }

  @Test
  public void testVerifyRule_EmptyDescription_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription( "" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( jobMeta );

    assertNotNull( feedbackList );
    assertFalse( feedbackList.isEmpty() );
    ImportValidationFeedback feedback = feedbackList.get( 0 );
    assertNotNull( feedback );
    assertEquals( ImportValidationResultType.ERROR, feedback.getResultType() );
    assertTrue( feedback.isError() );
  }

  @Test
  public void testVerifyRule_ShortDescription_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription( "short" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( jobMeta );

    assertNotNull( feedbackList );
    assertFalse( feedbackList.isEmpty() );
    ImportValidationFeedback feedback = feedbackList.get( 0 );
    assertNotNull( feedback );
    assertEquals( ImportValidationResultType.ERROR, feedback.getResultType() );
    assertTrue( feedback.isError() );
  }

  @Test
  public void testVerifyRule_SameAsMinimumLenghtDescription_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription(
      "1234567890" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( jobMeta );

    assertNotNull( feedbackList );
    assertFalse( feedbackList.isEmpty() );
    ImportValidationFeedback feedback = feedbackList.get( 0 );
    assertNotNull( feedback );
    assertEquals( ImportValidationResultType.APPROVAL, feedback.getResultType() );
    assertTrue( feedback.isApproval() );
  }

  @Test
  public void testVerifyRule_LongDescription_EnabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, true );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription(
      "A very long description that has more characters than the minimum required to be a valid one!" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( jobMeta );

    assertNotNull( feedbackList );
    assertFalse( feedbackList.isEmpty() );
    ImportValidationFeedback feedback = feedbackList.get( 0 );
    assertNotNull( feedback );
    assertEquals( ImportValidationResultType.APPROVAL, feedback.getResultType() );
    assertTrue( feedback.isApproval() );
  }

  @Test
  public void testVerifyRule_NullParameter_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_NotJobMetaParameter_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( "" );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_NullDescription_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription( null );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_EmptyDescription_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription( "" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_ShortDescription_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription( "short" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_SameAsMinimumLenghtDescription_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription(
      "1234567890" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  @Test
  public void testVerifyRule_LongDescription_DisabledRule() {
    JobHasDescriptionImportRule importRule = getImportRule( 10, false );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription(
      "A very long description that has more characters than the minimum required to be a valid one!" );

    List<ImportValidationFeedback> feedbackList = importRule.verifyRule( null );

    assertNotNull( feedbackList );
    assertTrue( feedbackList.isEmpty() );
  }

  private JobHasDescriptionImportRule getImportRule( int minLength, boolean enable ) {
    JobHasDescriptionImportRule importRule = new JobHasDescriptionImportRule();
    importRule.setMinLength( minLength );
    importRule.setEnabled( enable );

    return importRule;
  }
}
