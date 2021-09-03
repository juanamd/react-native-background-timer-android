class BackgroundTimer {
  static setTimeout() {
    return -1;
  }

  static setInterval() {
    return -1;
  }

  static clearTimeout() {
    return Promise.resolve();
  }

  static clearInterval() {
    return Promise.resolve();
  }
}

export default BackgroundTimer;
