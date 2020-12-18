package com.github.ksokol.mailsink.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterHolder {

  private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public void add(String subject, SseEmitter emitter) {
    emitters.computeIfAbsent(subject, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public void publish(String subject, Object message) {
    var sseEmitters = emitters.get(subject);
    if (sseEmitters == null) {
      return;
    }

    sseEmitters.parallelStream().forEach(sseEmitter -> {
      try {
        sseEmitter.send(message);
      } catch (Exception exception) {
        sseEmitters.remove(sseEmitter);
      }
    });
  }

  public int subscriberCount() {
    return emitters.values().size();
  }
}

