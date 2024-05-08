function fn() {
  var env = karate.env; // get system property 'karate.env'
  karate.log('karate.env system property was:', env);
  if (!env) {
    env = 'local';
  }
  var config = {};

  if (env == 'local') {
    config.baseUrl = 'http://localhost:8080'
    config.qPortalUrl = 'https://api.test.quincus.com'
  } else if (env == 'dev') {
    // customize
    // e.g. config.foo = 'bar';
  } else if (env == 'e2e') {
    // customize
  }

  var param = { baseUrl: config.baseUrl, qPortalUrl: config.qPortalUrl };
  var result = karate.callSingle('classpath:session/create.feature', param);
  config.token = result.token;
  config.utils = Java.type('com.quincus.karate.automation.utils.DataUtils');
  karate.configure('connectTimeout', 200000);
  karate.configure('readTimeout', 200000);
  return config;
}