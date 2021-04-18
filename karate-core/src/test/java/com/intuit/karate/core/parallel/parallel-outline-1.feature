@trigger-by-tag
Feature:

Background:
 # background http builder should work even for a dynamic scenario outline
 * url serverUrl
 * def data = [ { name: 'value1' }, { name: 'value2' }, { name: 'value3' }, { name: 'value4' } ]
 * def helloClass = Hello

Scenario Outline:
 * match functionFromKarateBase() == 'fromKarateBase'
 * path 'fromfeature'
 * method get
 * status 200
 * match response == { message: 'from feature' }
 * match helloClass.sayHello('from the other side') == 'hello from the other side'
 * match helloClass.sayHello(name) == 'hello ' + name

 Examples:
  | data |
