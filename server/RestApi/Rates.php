<?php
namespace RestApi;

class Rates {
	/**
	 * @return array
	 */
	public function getLast(){
		global $stockItems;
		
		$filter = 'RUB';
		
		$data = array();
		$mtime = 0;
		if(!empty($stockItems)){
			$data['fields'] = array_keys((array) $stockItems[0]);
			$data['values'] = array();
			foreach ($stockItems as $val){
				if( $val->currency === $filter ){
					$data['values'][] = array_values((array) $val);
					$mtime = max($mtime, $val->timestamp);
				}
			}
		}
		if (!HttpUtils::addHeader_LastModified($mtime, true)
// 				|| !HttpUtils::addHeader_expires(5 * 60)
				|| !HttpUtils::addHeader_ETag($data, true))
			die;
		return $data;
	}

}