# tag::imports[]
from micronaut.core.bind.annotation import Bindable
# end::imports[]


# tag::clazz[]
@Bindable  # <1>
def Correlation():
    def decorator(func):
        return func
    return decorator
# end::clazz[]
