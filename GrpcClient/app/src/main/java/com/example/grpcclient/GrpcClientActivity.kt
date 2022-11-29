package com.example.grpcclient

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.binder.AndroidComponentAddress
import io.grpc.binder.BinderChannelBuilder
import io.grpc.binder.LifecycleOnDestroyHelper
import io.grpc.binder.UntrustedSecurityPolicies
import io.grpc.stub.StreamObserver


class GrpcClientActivity : AppCompatActivity() {

    private val TAG = "GrpcClientActivity"
    private var mChannel: ManagedChannel? = null
    private val SERVICE_PACKAGE = "com.example.grpcserver"
    private val GRPC_CLASS = "com.example.grpcserver.GrpcServerService"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grpc_client)

        createChannel();

        generateRequest();
    }

    private fun createChannel() {
        val connectivityState =
            if (mChannel != null) mChannel!!.getState(false) else ConnectivityState.SHUTDOWN
        if (connectivityState != ConnectivityState.SHUTDOWN) {
            return
        }
        val mAddress =
            AndroidComponentAddress.forRemoteComponent(SERVICE_PACKAGE, GRPC_CLASS)
        val managedChannel = BinderChannelBuilder.forAddress(mAddress, this)
            .securityPolicy(UntrustedSecurityPolicies.untrustedPublic()).build()
        if (managedChannel == null) {
            Log.w(TAG, "create channel failed ")
            return
        }
        LifecycleOnDestroyHelper.shutdownUponDestruction(lifecycle, managedChannel)
        mChannel = managedChannel
        Log.i(TAG, "channel created: channel state " + mChannel!!.getState(false))
    }

    private fun generateRequest() {
        val request: GetBookRequest = GetBookRequest.newBuilder().build()
        val bookServiceGrpc: BookServiceGrpc.BookServiceStub =
            BookServiceGrpc.newStub(mChannel)
        bookServiceGrpc.getBook(
            request,
            object : StreamObserver<Book?> {
                override fun onNext(value: Book?) {
                    Log.i(TAG, "receive at onNext")
                    extractTheValues(value)
                }

                override fun onError(t: Throwable) {
                    Log.e(TAG, "receive on error $t")
                }

                override fun onCompleted() {
                    Log.i(TAG, "receive at completed")
                }
            })
    }

    private fun extractTheValues(book: Book?) {
        Log.i(TAG, "extracting the values :: $book")
        if (book != null) {
            Log.i(TAG, "Book Values are..\n" + book.isbn + "\n" + book.author + "\n" + book.title)
        }
    }
}