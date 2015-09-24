<?php
namespace Loader;

define('_COOKIE_LIFETIME', 7 * 24 * 60 * 60);
define('_HTTP_CODES_FILE', __DIR__ . '/http_codes.ini');

/**
 * @author Limych
 *
 */
abstract class BaseLoader {

	static $logFile = './loader.log';
	
	static $userAgent = 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36';
	
	static $cookieFile = './cookies.cache';
	static $cookieFileLifetime = _COOKIE_LIFETIME;	// One week

	static $httpCodesFile = _HTTP_CODES_FILE;
		
	static function log($msg){
		error_log(date('Y-m-d H:i:s') . "\t$msg\n", 3, self::$logFile);
	}
	
	static function makeTimestamp($datetime, $format = 'Y-m-d H:i:s'){
		try {
			$date = \DateTime::createFromFormat($format, $datetime);
		} catch (Exception $e) {
			self::log($e->getMessage());
		}
				
		if( empty($date) ){
			$date = new \DateTime('today');
		}
	
		return $date->getTimestamp();
	}

	/**
	 * Get a web file (HTML, XHTML, XML, image, etc.) from a URL.  Return an
	 * array containing the HTTP server response header fields and content.
	 */
	static function wget( $url ){
		// Delete cookie file periodically
		if( is_file(self::$cookieFile)
				&& time() > filemtime(self::$cookieFile) + self::$cookieFileLifetime ){
			unlink(self::$cookieFile);
			self::log('Internal cookie file deleted');
		}
	
		$options = array(
				CURLOPT_CUSTOMREQUEST  => 'GET',        //set request type post or get
				CURLOPT_POST           => false,        //set to GET
				CURLOPT_USERAGENT      => self::$userAgent,	//set user agent
				CURLOPT_COOKIEFILE     => self::$cookieFile, //set cookie file
				CURLOPT_COOKIEJAR      => self::$cookieFile, //set cookie jar
				CURLOPT_RETURNTRANSFER => true,     // return web page
				CURLOPT_HEADER         => false,    // don't return headers
				CURLOPT_FOLLOWLOCATION => true,     // follow redirects
				CURLOPT_ENCODING       => '',       // handle all encodings
				CURLOPT_AUTOREFERER    => true,     // set referer on redirect
				CURLOPT_CONNECTTIMEOUT => 120,      // timeout on connect
				CURLOPT_TIMEOUT        => 120,      // timeout on response
				CURLOPT_MAXREDIRS      => 10,       // stop after 10 redirects
		);
	
		$ch      = curl_init( $url );
		curl_setopt_array( $ch, $options );
		$content = curl_exec( $ch );
		$err     = curl_errno( $ch );
		$errmsg  = curl_error( $ch );
		$result  = curl_getinfo( $ch );
		curl_close( $ch );
	
		$result['errno']   = $err;
		$result['errmsg']  = $errmsg;
		$result['content'] = $content;
	
		if( $result['errno'] != 0 ){
			// error: bad url, timeout, redirect loop…
			self::log("HTTP-Request: $url");
			self::log("Error: ({$result[errno]}) {$result[errmsg]}");
	
		}elseif( $result['http_code'] != 200 ){
			// error: no page, no permissions, no service…
			static $http_codes = null;
			if($http_codes == null){
				$http_codes = parse_ini_file(self::$httpCodesFile);
			}
			$http_msg = ( $http_codes != null ? $http_codes[$result['http_code']] : "" );
			self::log("HTTP-Request: $url");
			self::log("HTTP-Error: ({$result[http_code]}) $http_msg");
		}
	
		return $result;
	}

	/**
	 * Factory.
	 *
	 * @return BaseLoader $this
	 */
	public static function create()	{
		$clazz = get_called_class();
		return new $clazz();
	}
	
	abstract function load();
}
