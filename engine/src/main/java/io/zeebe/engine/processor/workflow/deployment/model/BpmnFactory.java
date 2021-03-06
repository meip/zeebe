/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.processor.workflow.deployment.model;

import io.zeebe.el.ExpressionLanguage;
import io.zeebe.el.ExpressionLanguageFactory;
import io.zeebe.engine.processor.workflow.ExpressionProcessor;
import io.zeebe.engine.processor.workflow.deployment.model.transformation.BpmnTransformer;
import io.zeebe.engine.processor.workflow.deployment.transform.BpmnValidator;

public final class BpmnFactory {

  public static BpmnTransformer createTransformer() {
    return new BpmnTransformer(createExpressionLanguage());
  }

  public static BpmnValidator createValidator(final ExpressionProcessor expressionProcessor) {
    return new BpmnValidator(createExpressionLanguage(), expressionProcessor);
  }

  private static ExpressionLanguage createExpressionLanguage() {
    return ExpressionLanguageFactory.createExpressionLanguage();
  }
}
