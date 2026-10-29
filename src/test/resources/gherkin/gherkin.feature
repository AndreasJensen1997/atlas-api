Feature: Chapter API

Scenario: Successfully create a chapter
Given I have a valid authentication token
When I send a POST request to "/chapters"
Then I should receive a 201 Created response

Scenario: Retrieve a chapter that does not exist
Given I have a valid authentication token
When I send a GET request to "/chapters/9999"
Then I should receive a 404 Not Found response