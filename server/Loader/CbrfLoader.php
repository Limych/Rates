<?php
namespace Loader;

use Model;

class CbrfLoader extends BaseLoader {

	static function _parseXmlVal($data, $timestamp){
		$valute = array();
		for ($i=0; $i < count($data); $i++) {
			$valute[$data[$i]["tag"]] = $data[$i]["value"];
		}
		if(DEBUG) var_export($valute);
		
		$price = floatval(str_replace(',', '.', str_replace('.', '', $valute['Value'])));
		return new Model\StockItem(array(
				'source'	=> 'CBRF',
				'code'		=> $valute['CharCode'],
				'faceValue'	=> $valute['Nominal'],
				'currency'	=> 'RUB',
				'open'		=> $price,
				'last'		=> $price,
				'high'		=> $price,
				'low'		=> $price,
				'timestamp'	=> $timestamp,
		));
	}
	
	static function parseXml($xml){
		$parser = xml_parser_create ('');
		xml_parser_set_option ( $parser, XML_OPTION_CASE_FOLDING, 0 );
		xml_parser_set_option ( $parser, XML_OPTION_SKIP_WHITE, 1 );
		xml_parse_into_struct ( $parser, $xml, $values, $tags );
		xml_parser_free ( $parser );

// 		if(DEBUG){
// 			var_export($values);
// 			var_export($tags);
// 			die;
// 		}
		
		$timestamp = 0;
		$stockItems = array();
		foreach ( $tags as $key => $ranges ) {
			if ($key == 'ValCurs') {
				$timestamp = self::makeTimestamp($values[$ranges[0]]['attributes']['Date'], '!d.m.Y');
// 				if(DEBUG) var_export(Date('r', $timestamp));
				
			} elseif ($key == 'Valute') {
				for($i = 0; $i < count ( $ranges ); $i += 2) {
					$offset = $ranges [$i] + 1;
					$len = $ranges [$i + 1] - $offset;
					$stockItems[] = self::_parseXmlVal( array_slice ( $values, $offset, $len ), $timestamp );
				}
			}
		}
		return $stockItems;
	}
		
	public function load(){
		$url = 'http://www.cbr.ru/scripts/XML_daily.asp';
		
		$result_last = self::wget($url);
		if(DEBUG) var_export($result_last);
		
		$rates = array();
		if( $result_last['errno'] != 0 ){
			// error: bad url, timeout, redirect loop…
		
		}elseif( $result_last['http_code'] != 200 ){
			// error: no page, no permissions, no service…
		
		}else{
			$rates = self::parseXml($result_last['content']);
			if(DEBUG) var_export($rates);
return ;
			
			$json = preg_replace(array(
					'/^[^{]+/',
					'/[^}]+$/',
			), array(
					'',
					'',
			), $result['content']);
			$json = json_decode($json, true);
		
			$ids = $values = array();
			foreach($json['securities']['data'] as $val){
				if( substr($val[2], -4) == '_TOM' ){
					$ids[$val[0]]		= $val[2];
					$values[$val[0]]	= $val[6];
				}
			}
		
			foreach($json['marketdata']['data'] as $val){
				if( isset($ids[$val[20]]) && isset($val[7]) && isset($val[8]) ){
					$rates[] = new Model\StockItem(array(
						'source'	=> 'MOEX',
						'code'		=> substr($ids[$val[20]], 0, 3),
						'faceValue'	=> $values[$val[20]],
						'currency'	=> substr($ids[$val[20]], 3, 3),
						'open'		=> $val[7],
						'last'		=> $val[8],
						'high'		=> $val[5],
						'low'		=> $val[6],
						'timestamp'	=> self::makeTimestamp($val[39]),
					));
				}
			}
			// if(DEBUG) var_export($rates);
		}
		return $rates;
	}
}
