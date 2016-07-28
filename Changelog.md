# Hawaii Backend Core Changelog

## 0.3.9 28 July 2016, Paul Klos
 * Remove deprecated contructor on HttpRequestBuilder
 * Remove deprecated contructor on HawaiiExecutorImpl
 * Remove deprecated method ValidatableDomainObject.hasBeenValidated
 * Replace assertions on public method arguments
 * Replace deprecated jUnit Assert import
 * Explicitly call configure on DispatcherConfigurator
 * Remove deprecated constructor from ExecutorRepository
 * Remove deprecated RuntimeFeatures

## 0.3.8 27 July 2016, Jonathan Brouwers
 * Added functionality for Update/Delete/Insert in query framework

## 0.3.7 27 July 2016, Jonathan Brouwers
 * Added new ResponseHandler and new DbRequestBuilderRepository method using it.

## 0.3.6 25 July 2016, Jonathan Brouwers
 * Make QueryEnhancer part of DbRequestBuilder

## 0.3.5 25 July 2016, Paul Klos, Ronald Verheul
 * Add possibility to override Hawaii server home to maintain
   BPT, CSV and catalogue-related files externally
 * Remove assertions from ResultSetResponseHandlers

## 0.3.4 21 July 2016, Jonathan Brouwers
 * Added RespnseHandler with ResultSetExtractor

## 0.3.3 4 July 2016, Paul Klos
 * Fixed issue where queue statistics were not logged
 * Fixed type in DefaultLogManager

## 0.3.2 4 July 2016
 * Fixed issue with ScalarResponseHandler in case query returned no rows

## 0.3.1 8 Juni 2016, Ronald Verheul, Marcel Overdijk
 * Sitemap generator can now ignore pages based on list of regexp's.
 * Added flush method to cache service.

## 0.3.0 19 May 2016, Rutger Lubbers
 * Refactoring of async call framework (including database async calls)

## 0.2.19 3 May 2016, Jonathan Brouwers
 * Allow sending mails with multiple attachments

## 0.2.18 25 April 2016, Paul Klos
 * Allow changing queries without rebuilding and restarting

## 0.2.17 29 March 2016, Paul Klos
 * Limit no. of records to be retrieved in PaginationHelper

## 0.2.16 23 March 2016, Jonathan Brouwers, Wouter Eerdekens
 * Added unlimited expiration for ConcurrentMapCacheService.
 * Added HawaiiControllerExceptionHandler and NotFoundException.

## 0.2.15 17 February 2016, Paul Klos, Rutger Lubbers
* Added reload method to RuntimeFeaturesService
* Added FileDownload to stream files from backend to frontend.

## 0.2.14 25 November 2015, Rutger Lubbers
* Password masking for large passwords.

## 0.2.13 20 November 2015, Paul Klos
* Update PaginationHelper to take a JDBC Operations argument.

## 0.2.12 10 November 2015, Rutger Lubbers
* Inject JDBC Operations into Database repository, so a logging JDBC Operations can be configured.

## 0.2.11 21 October 2015, Wouter Eerdekens, Paul Klos
* Method getRuntimeFeatures(List<String> exclude) now also accepts categories instead of just single features.

## 0.2.10, 21 October 2015, Wouter Eerdekens, Paul Klos
* Extended RuntimeFeaturesService, created RuntimeFeaturesHolder.

## 0.2.9, 16 October 2015, Cedric Roijakkers
* Added optional feature to configure pages in the sitemap to be skipped.

## 0.2.8, 24 July 2015, Robby Pelssers
* Fixed bug with incorrect attachement name in mail sender.

## 0.2.7, 23 July 2015, Marcel Overdijk
* Added PropertyHasValue validator.
* Upgraded to Java 8.

## 0.2.6, 6 July 2015, Marcel Overdijk
* Added call ids response header (got lost after Jersey removal).

## 0.2.5, 19 Juni 2015, Marcel Overdijk
* Added pagination helpers to determine start and end index.

## 0.2.4, 15 Juni 2015, Wouter Eerdekens, Marcel Overdijk
* Added HandlerMethodAdapter to build a mapping of rest calls per controller.
* Added Apache Commons Lang 3.4 dependency.

## 0.2.3, 29 April 2015, Rutger Lubbers
* Added trace method to LogManager.

## 0.2.2, 24 April 2015, Hans Lammers, Kevin Beken, Marcel Overdijk
* Added Jackson object mapper.
* Added Spring Data Commons.
* Added Pagination support.

## 0.2.1, 20 April 2015, Marcel Overdijk
* Removed Jersey in favor of Spring MVC.

## 0.2.0, 17 April 2015, Marcel Overdijk
* Removed Jersey in favor of Spring MVC.

## 0.1.92, 17 Mar 2015, Rutger Lubbers
* Added xml bind date utility to use JodaTime in SOAP services / clients.
* Allow queries with block comments.

## 0.1.91, 16 Mrt 2015, Paul Klos
* Handle null attribute in @Mandatory validation.

## 0.1.90, 27 Feb 2015, Rutger Lubbers
* Output backend call ids in HTTP response headers.

## 0.1.89, 26 Feb 2015, Rutger Lubbers
* Output backend call ids in HTTP response headers.

## 0.1.88, 25 Feb 2015, Rutger Lubbers
* Cache - Added trace logging for memcached cacheservice.
* Request Dispatcher - Use configured time out from Java code before using the default of 10 seconds.

## 0.1.87, 13 Feb 2015, Paul Klos
* Move FeatureNotEnabledException from kahuna-backend
* Only add JSON object to array if it has at least one attribute

## 0.1.86, 06 Feb 2015, Paul Klos
* Support protocol errors on HawaiiValidation;

## 0.1.83, 05 Feb 2015, Rutger Lubbers
* Fixed pre-emptive authentication for HTTP calls;

## 0.1.82, 02 Feb 2015, Rutger Lubbers
* Added authentication to HTTP calls;

## 0.1.81, 02 Feb 2015, Rutger Lubbers
* Removed authorization errors from errors log.
* Removed thread already started exception for FileListeners.

## 0.1.80, 30 Jan 2015, Paul Klos
* Renamed ClassValidation to more generic HawaiiValidation to indicate it can also be used on fields.

## 0.1.79, 28 Jan 2015, Rutger Lubbers
* Core - Added ServerExceptionRuntimeWrapper

## 0.1.78, 29 Jan 2015, Richard den Adel

## 0.1.77, 28 Jan 2015, Rutger Lubbers
* Core - Log exceptions in debug level for controllers.
* JSON - Handle output of null values for Dates.
* JSON - Added output of BooleanProperty to JsonHelper utility.
* Cleanups - Added serial version ids to various objects.

## 0.1.76, 27 Jan 2015, Rutger Lubbers
* Added getSqlQuery method to AbstractDbRepository

## 0.1.75, 26 Jan 2015, Paul Klos
* Add method to write a JSON child object to a specific target class

## 0.1.74, 22 Jan 2015, Rutger Lubbers
* Fix for non returning SOAP requests with exceptions;
* Allow configuration of queue and time out for calls via Jolokia;

## 0.1.73, 20 Jan 2015, Rutger Lubbers
* Remember the SOAP response payload for logging purposes;
* Set correct status header for SOAP responsen;
* Cleanup of pom.xml

## 0.1.72, 19 Jan 2015, Paul Klos
* Prevent NPE when no value is set in OptionalPropertyValidator
* Add support for protocol errors from Optional validations
* Add support for class-level validation

## 0.1.71, 19 Jan 2015, Rutger Lubbers
* Report error when URL builder has no base URL set;

## 0.1.70, 14 Jan 2015, Paul Klos
* Add JSONArray as possible payload in HttpRequestBuilder

## 0.1.69, 9 Jan 2015, Rutger Lubbers
* Allow null values in domain property equal check.

## 0.1.68, 9 Jan 2015, Rutger Lubbers/Paul Klos
* Use uppercase for comparisons in EnumProperty

## 0.1.67, 9 Jan 2015, Rutger Lubbers/Paul Klos
* Add ValidatableDomainObject to support JSR-303 validations;
* Add check to see if domain object has been validated;

## 0.1.66, 8 Jan 2015, Rutger Lubbers
* Fix NullPointerException in JsonArrayResponseHandler;

## 0.1.65, 7 Jan 2015, Paul Klos
* Add method to parse JSON array to JsonConverter;

## 0.1.64, 7 Jan 2015, Paul Klos
* Add generic JSON writing classes;

## 0.1.63, 18 Dec 2014, Rutger Lubbers
* Configure input / output logging of individual calls (transaction types / call types);

## 0.1.62, 27 Nov 2014, Rutger Lubbers
* Configure and watch log4j via system property and/or environment setting

## 0.1.61, 17 Nov 2014, Rutger Lubbers
* Changed URL Builder so that double slashed are avoided when a call's base_url ends with a '/'.

## 0.1.60, 12 Nov 2014, Paul Klos
* Rename property nummer-behouden.sms.mail.to to sms.mail.to
* Hopefully fix issue of building it on windows (line separator fix)

## 0.1.59, 5 Nov 2014, Wouter Eerdekens
* fallback when no memcache

## 0.1.58, 5 Nov 2014, Wouter Eerdekens
* Added RuntimeFeatures
* Added HybridMemcachedCacheService

## 0.1.57, 29 Oct 2014, Rutger Lubbers, Ernst Jan Plugge
* Redone "When a service call fails, log the headers & body with WARN or ERROR level." of 0.1.56 :-)
* Log stacktraces the original log4j way.

## 0.1.56, 29 Oct 2014, Rutger Lubbers
* When a service call fails, log the headers & body with WARN or ERROR level.
* Allow setting of log levels.
* Log service calls in a separate categories, containing the call name.
* Refactoring - Introduced LogType enum for string constants.
* Refactoring - Removed deprecated method from log manager.

## 0.1.55, 29 Oct 2014, Wouter Eerdekens
* Added extra method to retrieve items from the cache

## 0.1.54, 28 Oct 2014, Rutger Lubbers
* Added queue statistics to the ENDCALL logging.

## 0.1.53, 28 Oct 2014, Marcel Overdijk
* Default response manager now logs tx.id as repsone header (must be enabled by setting the property: logging.context.transactionId)

## 0.1.52, 20 Oct 2014, Ernst Jan Plugge
* Make dispatcher configuration file name configurable

## 0.1.51, 17 Oct 2014, Rutger Lubbers
* Created MBean to set call logging max body sizes.

## 0.1.50, 13 Oct 2014, Rutger Lubbers
* Log queue sizes with INFO level
* Log cache performance with DEBUG level

## 0.1.49, 09 Oct 2014, Rutger Lubbers
* Added rejected task count to request dispatcher logging
* Enabled setting time-out of SOAP calls via dispatcher configuration

## 0.1.48, 08 Oct 2014, Rutger Lubbers
* Removed logging of stacktrace for a security exception

## 0.1.47, 08 Oct 2014, Rutger Lubbers
* Changed way of setting character encoding and mime type for backend requests

## 0.1.46, 25 Sep 2014, Rutger Lubbers
* Close log resources

## 0.1.45, 25 Sep 2014, Rutger Lubbers

* Log requests dispatcher queue capacity and load when dispatching a request

## 0.1.44, 23 Sep 2014, Rutger Lubbers

* Handle backend unavailable requests takes delayed execution into account (the priority queues)

## 0.1.43, 23 Sep 2014, Rutger Lubbers

* Configure request dispatchers' priority queue

## 0.1.42, 4 Sep 2014, Hans Lammers

* Handle backend unavailable requests

## 0.1.41, 2 Sep 2014, Ernst Jan Plugge

* Add masking functionality to the LogManager to mask passwords from logging.

## 0.1.40, 8 Aug 2014, Rutger Lubbers

* Log validation errors as normal JSON instead of a stacktrace.

## 0.1.39, 8 Aug 2014, Rutger Lubbers

* Log raw responses from backend systems when body cannot be parsed (is null).

## 0.1.37, 1 Aug 2014, Ernst Jan Plugge
## 0.1.38, 1 Aug 2014, Ernst Jan Plugge

* Two bug fixes discovered during release testing

## 0.1.36, 30 Jul 2014, Ernst Jan Plugge

* New logging infrastructure for machine-parseable logging

## 0.1.35, 23 Jul 2014, Wouter Eerdekens, Paul Klos

* Added SqlQueryService (moved from kahuna-backend)
* Added AbstractDBRepository (moved from kahuna-backend)

## 0.1.34, 14 Jul 2014, Wouter Eerdekens

* Added SOAPAction as methodName for SoapRequests.

## 0.1.33, 11 Jul 2014, Richard den Adel

* Connections in sms sender not closed, added close connection.

## 0.1.32, 11 Jul 2014, Wouter Eerdekens

* Added OutputStreamResponseHandler to throttling framework.

## 0.1.31, 10 Jul 2014, Rutger Lubbers

* Prevent double logging of HTTP input

## 0.1.30, 10 Jul 2014, Rutger Lubbers

* Do not output double log lines.

## 0.1.29, 10 Jul 2014, Rutger Lubbers

* Added logging of raw HTTP response.

## 0.1.28, 09 Jul 2014, Rutger Lubbers

* Fixed bug that logging does not show exceptions.

## 0.1.27, 07 Jul 2014, Rutger Lubbers

* Shutdown on threadpool (Added public stop method to ExecutorServiceRepository)

## 0.1.26, 07 Jul 2014, Paul Klos

* Update logging format to key/value pairs.

## 0.1.25, 07 Jul 2014, Rutger Lubbers

* Unload file change listener threads when unloading war.

## 0.1.24, 07 Jul 2014, Wouter Eerdekens

* Log HTTP headers for SOAP calls.
* Use LogManager instead of System.out.println for logging.

## 0.1.23, 07 Jul 2014, Wouter Eerdekens

* Quote value of SOAPAction header.

## 0.1.22, 04 Jul 2014, Wouter Eerdekens

* Send SOAPAction header.

## 0.1.21, 02 Jul 2014, Paul Klos

* Moved cache functionality from kahuna-backend to hawaii-backend-core.

## 0.1.20, 02 Jul 2014, Rutger Lubbers

* Fixed bug that SOAP errors get lost.
* Removed HawaiiHttpClient and JmxVersion.

## 0.1.19, 26 Jun 2014, Rutger Lubbers

* Changed ResponseManager to use varargs for JSONObjects and JSONSerializables.

## 0.1.18, 24 Jun 2014, Paul Klos

* Bumped version after merge.

## 0.1.17, 23 Jun 2014, Rutger Lubbers

* Get environment name and read property file for this environment.

## 0.1.16, 24 Jun 2014, Paul Klos

* Added HttpHeaderProvider to HttpRequestBuilder.

## 0.1.15, 20 Jun 2014, Wouter Eerdekens

* Temporarily disable Bus and throttling for SOAP requests.
* Updated Apache CXF to 2.7.11.

## 0.1.14, 18 Jun 2014, Wouter Eerdekens

* Added Content-Type header to SoapRequests.

## 0.1.13, 18 Jun 2014, Wouter Eerdekens

* Quick and dirty add of Context to SoapRequest.

## 0.1.12, 16 Jun 2014, Rutger Lubbers

* Allow abort of async calls & start with 'batch execution'.

## 0.1.11, 16 Jun 2014, Rutger Lubbers

* RequestDispatcher uses a (Hawaii) create thread before add to queue ThreadPoolExecutor.

## 0.1.10, 15 Jun 2014, Rutger Lubbers

* Allow setting of headers.
* Added response handlers.

## 0.1.9, 13 Jun 2014, Rutger Lubbers

* Excluded mail client from cxf as dependency.

## 0.1.8, 13 Jun 2014, Rutger Lubbers

* Moved call throttling from SSEFE into backend core.

## 0.1.7, 13 Jun 2014, Rutger Lubbers

* Bumped version of Jettison to 1.3.5 to fix a problem reading null values in JSON.

## 0.1.6, 11 Jun 2014, Paul Klos, Pieter Otten, Rutger Lubbers

* Cleanup errors list (PK)
* Use test-specific errors for unit tests (PK)
* Corrected method name in HawaiiItemValidationError (PK)
* Separate test-specific errors (PK)
* Remove shop-specific error definitions (PK)
* Separate out test-specific error definition (PK)
* Added CACI_ERROR and a logger for CLI project (PO)
* Use defined property for jolokia version in the dependency definition (RL)

## 0.1.5, 06 Jun 2014, Paul Klos

* Added constructor from unify-b-stream to ValidationException.
* Make ServerException use interface instead of enum directly.
* Make ValidationException use interface isntead of enum directly.
* Added sendHtmlMail method to MailSender.

## 0.1.4, 04 Jun 2014, Paul Klos

* Changed LogManager to use interface for logger name.

## 0.1.3, 26 May 2014, Paul Klos

* Added logger name.

## 0.1.2, 27 May 2014, Paul Klos

* Commit on feature branch.
* Updated Jersey to 1.18.
* Updated Apache HttpClient to 4.3.3.

## 0.1.2, 27 May 2014, Hans Lammers

* Production fix in FileChangeListener.

## 0.1.1, 20 May 2014, Hans Lammers

* Set Java version from 1.7 to 1.6.

## 0.1.0, 05 May 2014, Hans Lammers

* Initial release.
