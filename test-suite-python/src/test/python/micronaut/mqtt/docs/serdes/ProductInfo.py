# tag::clazz[]
class ProductInfo:

    def __init__(self,
                 size: str | None,  # <1>
                 count: int,  # <2>
                 sealed: bool):  # <3>
        self.size = size
        self.count = count
        self.sealed = sealed
# end::clazz[]
