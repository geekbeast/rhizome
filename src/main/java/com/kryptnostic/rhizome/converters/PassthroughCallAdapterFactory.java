package com.kryptnostic.rhizome.converters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class PassthroughCallAdapterFactory extends CallAdapter.Factory {

    @Override
    public CallAdapter<?> get( Type returnType, Annotation[] annotations, Retrofit retrofit ) {
        return new CallAdapter<Object>() {
            @Override
            public Type responseType() {
                return returnType;
            }

            @Override
            public <R> Object adapt(Call<R> call) {
                try {
                    return call.execute().body();
                } catch (IOException e) {
                    return null;
                }
            }

        };
    }

}
