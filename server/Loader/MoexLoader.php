<?php
namespace Loader;

use Model;

class MoexLoader extends BaseLoader {
	
	public function load(){
		$hash = sha1(date('r'));
		$timestamp = time();
		$url = "http://www.moex.com/iss/engines/currency/markets/selt/securities.json?iss.meta=off&iss.only=securities%2Cmarketdata&securities=CETS%3AUSD000UTSTOM%2CCETS%3AEUR_RUB__TOM%2CCETS%3AEURUSD000TOM%2CCETS%3ACNYRUB_TOM%2CCETS%3AKZT000000TOM%2CCETS%3AUAH000000TOM%2CCETS%3ABYRRUB_TOM%2CCETS%3AHKDRUB_TOM%2CCETS%3AGBPRUB_TOM&lang=ru&_=$timestamp";
		
		$result = self::wget($url);
// 		if(DEBUG) var_export($result);
		
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
