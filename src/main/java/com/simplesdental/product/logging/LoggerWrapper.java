package com.simplesdental.product.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LoggerWrapper {
  private final Logger logger;

  private static final Marker CRITICAL = MarkerFactory.getMarker("CRITICAL");

  public LoggerWrapper(Class<?> clazz) {
    this.logger = LoggerFactory.getLogger(clazz);
  }

  public void debug(String message, Object... args) {
    logger.debug(message, args);
  }

  public void info(String message, Object... args) {
    logger.info(message, args);
  }

  public void warn(String message, Object... args) {
    logger.warn(message, args);
  }

  public void error(String message, Object... args) {
    logger.error(message, args);
  }

  public void critical(String message, Object... args) {
    logger.error(CRITICAL, message, args);
  }
}
