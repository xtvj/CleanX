package github.xtvj.cleanx.data

//@ExperimentalPagingApi
//class AppRemoteMediator(
//    private val appItemDao: AppItemDao,
//    private val db: AppDatabase,
//    private val pm: PackageManager,
//    private val code: String
//) :
//    RemoteMediator<Int, AppItem>() {
//
//    override suspend fun initialize(): InitializeAction {
//        // Require that remote REFRESH is launched on initial load and succeeds before launching
//        //默认就是此值
//        return InitializeAction.LAUNCH_INITIAL_REFRESH
//    }
//
//    override suspend fun load(
//        loadType: LoadType,
//        state: PagingState<Int, AppItem>
//    ): MediatorResult {
//
//        //RemoteMediator适用于网络请求，不适用于本地获取数据，此处只是简单了解RemoteMediator
//        try {
//
//            when (loadType) {
//                LoadType.PREPEND -> {
//                    return MediatorResult.Success(true)
//                }
//                LoadType.APPEND -> {
//                    return MediatorResult.Success(true)
//                }
//                else -> {}
//            }
//
//
//            log("RemoteMediator loadType: ${loadType.name}")
//
//            val list = GetApps.getAppsByCode(pm, code)
//
//            db.withTransaction {
//                log("RemoteMediator code: $code")
//                if (loadType == LoadType.REFRESH) {
//                    when (code) {
//                        GET_USER -> {
//                            appItemDao.deleteAllUser()
//                        }
//                        GET_SYS -> {
//                            appItemDao.deleteAllSystem()
//                        }
//                        GET_DISABLED -> {
//                            appItemDao.deleteAllDisable()
//                        }
//                    }
//                }
//                appItemDao.insertMultipleItems(list)
//            }
//            return MediatorResult.Success(true)
//
//        } catch (exception: IOException) {
//            return MediatorResult.Error(exception)
//        }
//    }
//
//}