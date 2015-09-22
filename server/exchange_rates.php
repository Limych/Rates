<?php
define('BASENAME',		preg_replace('/(.+)\..*$/', '$1', basename(__FILE__)));

define('CACHE_FILE',	BASENAME . '.hist');
define('CACHE_LIFE',	5 * 60);	// 5 min

define('INTERNAL_COOKIE_FILE',	'cookies.txt');
define('INTERNAL_COOKIE_LIFE',	10000);

define('HISTORY_FILE',	BASENAME . '.history');

define('HTTP_CODES_FILE',	dirname(BASENAME) . '/http_codes.ini');

define('LOG_FILE',	BASENAME . '.log');



header('Content-Type: text/plain; charset=UTF-8');

if( isset($_REQUEST['debug']) ){
	define('DEBUG', true);
	error_reporting(E_ALL);
	ini_set('display_errors', true);

	// print json_encode($exrates->getLastRates());
	
	die();
}
define('DEBUG', false);

if( file_exists(CACHE_FILE) ){
	$exrates = unserialize(file_get_contents(CACHE_FILE));
}else{
	$exrates = new ExRates();
}
if( !file_exists(CACHE_FILE) || ( (filemtime(CACHE_FILE) + CACHE_LIFE) < time() ) ){
	update_rates();
	// if(DEBUG) app_log(var_export($rates, true));
	
	file_put_contents(CACHE_FILE, serialize($exrates));
	app_log('Rates updated');
}

$timestamp = filemtime(CACHE_FILE);
$tsstring = gmdate('D, d M Y H:i:s ', $timestamp) . 'GMT';
$etag = md5($timestamp);

$if_modified_since = !isset($_SERVER['HTTP_IF_MODIFIED_SINCE']) ? false : $_SERVER['HTTP_IF_MODIFIED_SINCE'];
$if_none_match = !isset($_SERVER['HTTP_IF_NONE_MATCH']) ? false : $_SERVER['HTTP_IF_NONE_MATCH'];
if ( ( !$if_none_match || ($if_none_match && $if_none_match == $etag) ) &&
		($if_modified_since && $if_modified_since == $tsstring) ) {
    header('HTTP/1.1 304 Not Modified');
    exit;
} else {
    header("Last-Modified: $tsstring");
    header("ETag: \"$etag\"");

	print json_encode($exrates->getLastRates());
}



class ExRates {
	public $rates;
	
	function __construct(){
		$this->rates = array();
	}
	
	function setRate($timestamp, $group, $currency, $good, $face_value, $bid_open, $bid_last, $bid_high = -1, $bid_low = -1){
		$group = strtoupper($group);
		$currency = strtoupper($currency);
		$good = strtoupper($good);
		$date = date('Y_z', $timestamp);
		
		if( !is_array($this->rates) ){
			$this->rates = array();
		}
		if( !isset($this->rates[$group]) || !is_array($this->rates[$group]) ){
			$this->rates[$group] = array();
			ksort($this->rates);
		}
		if( !isset($this->rates[$group][$good]) || !is_array($this->rates[$group][$good]) ){
			$this->rates[$group][$good] = array();
			ksort($this->rates[$group]);
		}
		if( !isset($this->rates[$group][$good][$currency]) || !is_array($this->rates[$group][$good][$currency]) ){
			$this->rates[$group][$good][$currency] = array();
			ksort($this->rates[$group][$good]);
		}
		if( !isset($this->rates[$group][$good][$currency][$date])
				|| !is_array($this->rates[$group][$good][$currency][$date])
				|| $this->rates[$group][$good][$currency][$date][3] < $timestamp ){
					
			$this->rates[$group][$good][$currency][''] = $date;
			$this->rates[$group][$good][$currency][$date] = array(
				$group,			// 0
				$good,			// 1
				$currency,		// 2
				$timestamp,		// 3
				$face_value,	// 4
				$bid_open,		// 5
				$bid_last,		// 6
				$bid_high,		// 7
				$bid_low,		// 8
			);
		}
	}

	function getLastRates(){
		$last_rates = array();
		foreach($this->rates as $group_key => $groups){
			foreach($groups as $good_key => $goods){
				foreach($goods as $currency_key => $curr){
					$last_rates[$group_key.'_'.$good_key.'_'.$currency_key] = $curr[$curr['']];
				}
			}
		}
		return $last_rates;
	}
}



function make_timestamp($datetime, $format = 'Y-m-d H:i:s'){
	try {
		$date = DateTime::createFromFormat($format, $datetime);
	} catch (Exception $e) {
		$msg = $e->getMessage();
		app_log($msg);
		if(DEBUG) var_export($msg);
	}
	
	if( empty($date) ){
		$date = new DateTime('today');
	}
	
	return $date->getTimestamp();
}



function app_log($msg){
	if(DEBUG){
		$msg = "DEBUG\t" . $msg;
	}
	error_log(date('Y-m-d H:i:s') . "\t$msg\n", 3, LOG_FILE);
}



function fix_rates($old_rates){
	$values = array();
	$rates = $old_rates;
	foreach( $old_rates as $key => $val ){
		$from = substr($key, 0, 3);
		$new_key = substr($key, 3, 3) . $from . substr($key, 6);
		if( $from == "RUB" && !isset($rates[$new_key]) ){
			$key = $new_key;
			$val[3] = 1 / $val[3];
			$val[4] = 1 / $val[4];
			$rates[$key] = $val;
		}
	}

	foreach( $rates as $key => $val ){
		$key = substr($key, 0, 3);
		if( $values[$key] < $val[2] )
			$values[$key] = $val[2];
	}
	foreach( $rates as $key => $val ){
		$from = substr($key, 0, 3);
		if( isset($values[$from]) && ($values[$from] != $val[2]) ){
			$val[3] = $val[3] * $values[$from] / $val[2];
			$val[4] = $val[4] * $values[$from] / $val[2];
			$val[2] = $values[$from];
			$rates[$key] = $val;
		}
	}
	return $rates;
}



function update_rates(){
	global $exrates;
	
	$update = array();
	$update = array_merge($update, get_moex_rates());
	$update = array_merge($update, get_cbr_rates());
	$update = array_merge($update, get_forex_rates());
	$update = array_merge($update, get_commodities_rates());

	$update = fix_rates($update);

	// app_log(var_export($update, true));
	foreach($update as $key => $val){
		$exrates->setRate($val[1], $val[0], $val[7], $val[8], $val[2], $val[3], $val[4], $val[5], $val[6]);
	}
}



/**
 * Get a web file (HTML, XHTML, XML, image, etc.) from a URL.  Return an
 * array containing the HTTP server response header fields and content.
 */
function get_web_page( $url ){
	$user_agent='Mozilla/5.0 (Windows NT 6.1; rv:8.0) Gecko/20100101 Firefox/8.0';

	// Delete cookie file periodically
	if( rand(0, INTERNAL_COOKIE_LIFE) === 0 ){
		unlink(INTERNAL_COOKIE_FILE);
		app_log('Internal cookie file deleted');
	}
	
	$options = array(
		CURLOPT_CUSTOMREQUEST  => 'GET',        //set request type post or get
		CURLOPT_POST           => false,        //set to GET
		CURLOPT_USERAGENT      => $user_agent,	//set user agent
		CURLOPT_COOKIEFILE     => INTERNAL_COOKIE_FILE, //set cookie file
		CURLOPT_COOKIEJAR      => INTERNAL_COOKIE_FILE, //set cookie jar
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
	
	// app_log("HTTP-Request: $url");
	if( $result['errno'] != 0 ){
		// error: bad url, timeout, redirect loop…
		app_log("HTTP-Request: $url");
		app_log("Error: ({$result[errno]}) {$result[errmsg]}");

	}elseif( $result['http_code'] != 200 ){
		// error: no page, no permissions, no service…
		static $http_codes = null;
		if($http_codes == null){
			$http_codes = parse_ini_file(HTTP_CODES_FILE);
		}
		$http_msg = $http_codes[$result['http_code']];
		app_log("HTTP-Request: $url");
		app_log("HTTP-Error: ({$result[http_code]}) $http_msg");
	}

	return $result;
}



function get_moex_rates(){
	$hash = sha1(date('r'));
	$timestamp = time();
	$url = "http://www.moex.com/iss/engines/currency/markets/selt/securities.json?iss.meta=off&iss.only=securities%2Cmarketdata&securities=CETS%3AUSD000UTSTOM%2CCETS%3AEUR_RUB__TOM%2CCETS%3AEURUSD000TOM%2CCETS%3ACNYRUB_TOM%2CCETS%3AKZT000000TOM%2CCETS%3AUAH000000TOM%2CCETS%3ABYRRUB_TOM%2CCETS%3AHKDRUB_TOM%2CCETS%3AGBPRUB_TOM&lang=ru&_=$timestamp";

	$result = get_web_page($url);
	// if(DEBUG) var_export($result);

	$rates = array();
	if( $result['errno'] != 0 ){
		// error: bad url, timeout, redirect loop…

	}elseif( $result['http_code'] != 200 ){
		// error: no page, no permissions, no service…

	}else{
		$json = preg_replace(array(
				'/^[^{]+/',
				'/[^}]+$/',
			), array(
				'',
				'',
			), $result['content']);
		$json = json_decode($json, true);
		// if(DEBUG) var_export($json);

		$ids = $values = array();
		foreach($json['securities']['data'] as $val){
			if( substr($val[2], -4) == '_TOM' ){
				$ids[$val[0]]		= $val[2];
				$values[$val[0]]	= $val[6];
			}
		}
		// if(DEBUG) var_export($ids);

		foreach($json['marketdata']['data'] as $val){
			if( isset($ids[$val[20]]) && isset($val[7]) && isset($val[8]) ){
				$rates[$ids[$val[20]]] = array(	// Cross name
					"STK",				// 0 - Cross type
					make_timestamp($val[39]),			// 1 - Timestamp
					$values[$val[20]],	// 2 - Face value
					$val[7],			// 3 - Bid on open
					$val[8],			// 4 - Last bid
					$val[5],			// 5 - High bid
					$val[6],			// 6 - Low bid
					substr($ids[$val[20]], 3, 3),	// 7 - Cross curr
					substr($ids[$val[20]], 0, 3),	// 8 - Cross good
				);
			}
		}
		// if(DEBUG) var_export($rates);
	}
	return $rates;
}



function cbr_parse_table($content){
	preg_match('|<h2>Центральный банк Российской Федерации установил с ([\d\.]+)|uSis', $content, $m);
	// if(DEBUG) var_export($m[0]);
	// Convert dd.mm.yyyy to yyyy-mm-dd
	$timestamp = make_timestamp($m[1], 'd.m.Y');

	preg_match('|<table class="data">.+?</table>|uSis', $content, $m);
	// if(DEBUG) var_export($m[0]);

	preg_match_all('|<tr><td>.+?</td>\s*<td>(.+?)</td>\s*<td>(.+?)</td>\s*<td>.+?</td>\s*<td>(.+?)</td>\s*|uSis', $m[0], $matches, PREG_SET_ORDER);
	// if(DEBUG) var_export($matches);
	
	$rates = array();
	foreach($matches as $m){
		$rates[$m[1] . 'RUB_CBR'] = array(	// Cross name
			"CBR",								// Cross type
			$timestamp,							// Timestamp
			intval($m[2]),						// Face value
			floatval(strtr($m[3], ',', '.')),	// Bid
		);
	}
	// if(DEBUG) var_export($rates);
	// if(DEBUG) app_log(var_export($rates, true));

	return $rates;
}



// Альтернатива: http://www.cbr.ru/scripts/XML_daily.asp?date_req=07.09.2015
// и для последней даты http://www.cbr.ru/scripts/XML_daily.asp
function get_cbr_rates(){
	$url = "http://www.cbr.ru/currency_base/daily.aspx?date_req=";

	$result_open = get_web_page($url . date("d.m.Y"));
	$result_last = get_web_page($url . date("d.m.Y", time() + 86400));
	// if(DEBUG) var_export($result_open);
	// if(DEBUG) var_export($result_last);
	
	$rates = array();
	if( $result_open['errno'] != 0 ){
		// error: bad url, timeout, redirect loop…

	}elseif( $result_open['http_code'] != 200 ){
		// error: no page, no permissions, no service…

	}else{
		$rates = cbr_parse_table($result_open['content']);
		// if(DEBUG) var_export($rates);
		
		$tmp = cbr_parse_table($result_last['content']);
		foreach($tmp as $key => $val){
			// If new CBR rates not set, leave last rates unchanged
			if( $rates[$key][1] == $val[1] ){
				app_log('Tomorrow CBR rates for ' . $key . ' currently are not set.');
				unset($rates[$key]);
			}
			
			$rates[$key][4] = $val[3];							// Last bid
			$rates[$key][5] = max($val[3], $rates[$key][3]);	// High bid
			$rates[$key][6] = min($val[3], $rates[$key][3]);	// Low bid
			$rates[$key][7] = substr($key, 3, 3);				// 7 - Cross curr
			$rates[$key][8] = substr($key, 0, 3);				// 8 - Cross good
		}
		// if(DEBUG) var_export($rates);
		// if(DEBUG) app_log(var_export($rates, true));
	}
	return $rates;
}



// Альтернатива: http://partners.instaforex.com/ru/quotes_description.php/
function get_forex_rates(){
	$url = "http://fxrates.ru.forexprostools.com/index_single_crosses.php?currency=79&bid=hide&ask=hide&last=show&change=hide&change_in_percents=hide&last_update=show";

	$result = get_web_page($url);
	// if(DEBUG) var_export($result);

	$rates = array();
	if( $result['errno'] != 0 ){
		// error: bad url, timeout, redirect loop…

	}elseif( $result['http_code'] != 200 ){
		// error: no page, no permissions, no service…

	}else{
		preg_match('|<table id="cross_rate_1".+?</table>|uSis', $result['content'], $m);
		// if(DEBUG) var_export($m[0]);

		preg_match_all('|<span class="ftqa11bb arial_11_b">([^<]+)</span></td></nobr><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td></tr>|uSis', $m[0], $matches, PREG_SET_ORDER);
		// if(DEBUG) var_export($matches);
		// if(DEBUG) app_log(var_export($matches, true));
		
		foreach($matches as $m){
			$rates[str_replace('/', '', $m[1]) . '_FRX'] = array(	// Cross name
				"FRX",												// 0 — Cross type
				make_timestamp($m[6], 'H:i:s'),						// 1 — Timestamp
				1,													// 2 — Face value
				floatval($m[3]),									// 3 — Bid on open
				floatval($m[2]),									// 4 — Last bid
				floatval($m[4]),									// 5 — High bid
				floatval($m[5]),									// 6 — Low bid
				substr($m[1], 4, 3),								// 7 — Cross curr
				substr($m[1], 0, 3),								// 8 — Cross good
			);
		}
		// if(DEBUG) var_export($rates);
	}
	return $rates;
}



// Альтернатива: https://www.quandl.com/resources/commodity-data#Commodity-Dashboards
function get_commodities_rates(){
	$url = "http://comrates.investing.com/index.php?force_lang=1&pairs_ids=8830;8831;8833;8836;8910;&open=show&month=hide&change=hide&change_in_percents=hide&last_update=show";

	$result = get_web_page($url);
	// if(DEBUG) var_export($result);

	$rates = array();
	if( $result['errno'] != 0 ){
		// error: bad url, timeout, redirect loop…

	}elseif( $result['http_code'] != 200 ){
		// error: no page, no permissions, no service…

	}else{
		preg_match('|<table id="cross_rate_1".+?</table>|uSis', $result['content'], $m);
		// if(DEBUG) var_export($m[0]);

		preg_match_all('|<span class="ftqa11bb arial_11_b">([^<]+)</span></td></nobr><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td><td class="[^>]+>([^<]+)</td></tr>|uSis', $m[0], $matches, PREG_SET_ORDER);
		// if(DEBUG) var_export($matches);
		// if(DEBUG) app_log(var_export($matches, true));
		
		static $codes = array(
			'Gold'		=> 'GCZ15',
			'Silver'	=> 'SIQ15',
			'Copper'	=> 'HGQ15',
			'Platinum'	=> 'PLQ15',
			'Brent&nbsp;Oil'	=> 'LCOV5',
		);
		
		foreach($matches as $m){
			$rates[$codes[$m[1]] . '_CMM'] = array(	// Cross name
				"CMM",							// 0 — Cross type
				make_timestamp($m[6], 'H:i:s'),	// 1 — Timestamp
				1,								// 2 — Face value
				floatval($m[3]),				// 3 — Bid on open
				floatval($m[2]),				// 4 — Last bid
				floatval($m[4]),				// 5 — High bid
				floatval($m[5]),				// 6 — Low bid
				'USD',							// 7 — Cross curr
				$codes[$m[1]],					// 8 — Cross good
			);
		}
		// if(DEBUG) var_export($rates);
	}
	return $rates;
}
