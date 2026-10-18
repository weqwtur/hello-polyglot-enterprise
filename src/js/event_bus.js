'use strict';

/** Tiny synchronous event bus. */
class EventBus {
  constructor() {
    this.handlers = new Map();
    this.published = 0;
  }

  on(topic, handler) {
    if (!this.handlers.has(topic)) {
      this.handlers.set(topic, []);
    }
    this.handlers.get(topic).push(handler);
  }

  emit(topic, payload) {
    this.published += 1;
    const list = this.handlers.get(topic) || [];
    for (const handler of list) {
      handler(payload);
    }
  }
}

module.exports = { EventBus };
