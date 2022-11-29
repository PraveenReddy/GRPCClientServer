package com.example.grpcserver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.grpc.Book
import com.example.grpc.BookServiceGrpc
import com.example.grpc.GetBookRequest
import io.grpc.Server
import io.grpc.binder.*
import io.grpc.stub.StreamObserver
import java.io.IOException


class GrpcServerService : Service() {
    private var mContext: Context? = null
    private val TAG: String = "GrpcServerService"
    private var mServer: Server? = null
    private val mBinderReceiver = IBinderReceiver()

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "binding and returning -> " + mBinderReceiver.get());
        return mBinderReceiver.get();
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this@GrpcServerService
        val address = AndroidComponentAddress.forContext(this)
        Log.i(TAG, "onCreate: listening address: $address")
        try {
            mServer = BinderServerBuilder.forAddress(address, mBinderReceiver)
                .addService(BookServiceImpl())
                .securityPolicy(
                    ServerSecurityPolicy.newBuilder() // Signature/uid policies could be added
                        .servicePolicy(
                            BookServiceGrpc.SERVICE_NAME,
                            UntrustedSecurityPolicies.untrustedPublic()
                        )
                        .build()
                )
                .build()
                .start()
            Log.i(TAG, "onCreate: server started: " + BookServiceGrpc.SERVICE_NAME)
        } catch (e: IOException) {
            Log.i(TAG, "onCreate: failed to start server: " + BookServiceGrpc.SERVICE_NAME)
        }
    }

    class BookServiceImpl :
        BookServiceGrpc.BookServiceImplBase() {
        override fun getBook(
            request: GetBookRequest?,
            responseObserver: StreamObserver<Book?>?
        ) {
            Log.i("GrpcServerService", "receive at BookServiceImpl")
            val response: Book.Builder = Book.newBuilder()
            response.isbn = 12345
            response.author = "Praveen"
            response.title = "Algorithms"

            responseObserver!!.onNext(response.build())
            responseObserver!!.onCompleted()
            Log.i("GrpcServerService", "response observer sent for completed")
        }
    }
}