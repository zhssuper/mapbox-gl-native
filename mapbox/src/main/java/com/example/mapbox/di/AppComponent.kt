import com.mapbox.mapboxsdk.wearapp.MainActivityModule
import com.mapbox.mapboxsdk.wearapp.MapboxApp
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Application component refers to application level modules only
 */
@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    MainActivityModule::class
])
interface AppComponent : AndroidInjector<MapboxApp> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<MapboxApp>()
}