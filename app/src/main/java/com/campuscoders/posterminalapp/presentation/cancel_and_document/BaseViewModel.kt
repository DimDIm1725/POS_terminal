package com.campuscoders.posterminalapp.presentation.cancel_and_document

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campuscoders.posterminalapp.domain.model.Orders
import com.campuscoders.posterminalapp.domain.model.OrdersProducts
import com.campuscoders.posterminalapp.domain.use_case.cancel_and_document.FetchLatestSuccessfulSaleUseCase
import com.campuscoders.posterminalapp.domain.use_case.cancel_and_document.FetchOrderByMaliIdUseCase
import com.campuscoders.posterminalapp.domain.use_case.cancel_and_document.FetchOrderByReceiptNoUseCase
import com.campuscoders.posterminalapp.domain.use_case.cancel_and_document.FetchOrderByTerminalIdUseCase
import com.campuscoders.posterminalapp.domain.use_case.cancel_and_document.FetchOrderProductsByOrderIdUseCase
import com.campuscoders.posterminalapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor (
    private val fetchOrderByReceiptNoUseCase: FetchOrderByReceiptNoUseCase,
    private val fetchOrderByMaliIdUseCase: FetchOrderByMaliIdUseCase,
    private val fetchOrderByTerminalIdUseCase: FetchOrderByTerminalIdUseCase,
    private val fetchLatestSuccessfulSaleUseCase: FetchLatestSuccessfulSaleUseCase,
    private val fetchOrderProductsByOrderIdUseCase: FetchOrderProductsByOrderIdUseCase
): ViewModel() {

    private var _statusOrderDetail = MutableLiveData<Resource<Orders>>()
    val statusOrderDetail: LiveData<Resource<Orders>>
        get() = _statusOrderDetail

    private var _statusProductAndTaxPrice = MutableLiveData<HashMap<String,String>>()
    val statusProductAndTaxPrice: LiveData<HashMap<String,String>>
        get() = _statusProductAndTaxPrice

    fun querySale(searchType: String, searchKey: String) {
        _statusOrderDetail.value = Resource.Loading(null)
        viewModelScope.launch {
            when(searchType) {
                "receiptNo" -> {
                    val response = fetchOrderByReceiptNoUseCase.executeFetchOrderByReceiptNo(searchKey)
                    _statusOrderDetail.value = response
                    fetchOrdersProductsList()
                }
                "orderMaliId" -> {
                    val response = fetchOrderByMaliIdUseCase.executeFetchOrderByMaliId(searchKey)
                    _statusOrderDetail.value = response
                    fetchOrdersProductsList()
                }
                "terminalId" -> {
                    val response = fetchOrderByTerminalIdUseCase.executeFetchOrderByTerminal(searchKey)
                    _statusOrderDetail.value = response
                    fetchOrdersProductsList()
                }
            }
        }
    }

    fun fetchLatestSuccessfulSale() {
        _statusOrderDetail.value = Resource.Loading(null)
        viewModelScope.launch {
            val response = fetchLatestSuccessfulSaleUseCase.executeFetchLatestSuccessfulSale()
            _statusOrderDetail.value = response
        }
    }

    private fun fetchOrdersProductsList() {
        viewModelScope.launch {
            if (_statusOrderDetail.value is Resource.Success) {
                val order = (_statusOrderDetail.value as Resource.Success<Orders>).data
                val responseOrdersProductsList = fetchOrderProductsByOrderIdUseCase.executeFetchOrderProductsByOrderId(order?.orderId?.toString()?:"0")
                calculateTotalPriceAndTax(responseOrdersProductsList)
            }
        }
    }

    private fun calculateTotalPriceAndTax(ordersProductsList: Resource<List<OrdersProducts>>) {
        val hashMap = hashMapOf<String, String>()
        if (ordersProductsList is Resource.Success) {
            var totalPrice = 0
            var totalPriceCent = 0
            var totalTax = 0
            var totalTaxCent = 0
            for (i in ordersProductsList.data!!) {
                totalPrice += i.orderProductsPrice!!.toInt()
                totalPriceCent += i.orderProductsPriceCents!!.toInt()
                totalTax += i.orderProductsKdvPrice!!.toInt()
                totalTaxCent += i.orderProductsKdvPriceCents!!.toInt()
            }
            totalPrice += totalPriceCent / 100
            totalTax += totalTaxCent / 100
            hashMap["price"] = "$totalPrice,${totalPriceCent % 100}"
            hashMap["tax"] = "$totalTax,${totalTaxCent % 100}"
            _statusProductAndTaxPrice.value = hashMap
        }
        else {
            hashMap["price"] = "null"
            hashMap["tax"] = "null"
            _statusProductAndTaxPrice.value = hashMap
        }
    }
}