@apache
Feature: test url-encoded form-field submissions

Scenario: should be able to over-ride the content-type
    Given url demoBaseUrl
    And path 'search', 'headers'
    And form field text = 'hello'
    And header Content-Type = 'application/json'
    When method post
    Then status 200
    And match response['content-type'][0] contains 'application/json'

