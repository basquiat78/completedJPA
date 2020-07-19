package io.basquiat.model.item;

public enum OrderStatus {

	/** 출고 준비중 */
	RELEASEREADY,
	
	/** 출고 완료 */
	RELEASED,
	
	/** 주문 완료 */
	COMPLETE,
	
	/** 구매 확정 */
	ENDCOMPLETE,
	
	/** 반품 */
	RETURN,
	
	/** 교환 */
	EXCHANGE,
	
	/** 주문 취소 */
	CANCWEL;
}
