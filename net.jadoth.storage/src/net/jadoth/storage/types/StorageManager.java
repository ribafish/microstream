package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.storage.exceptions.StorageExceptionNotAcceptingTasks;
import net.jadoth.storage.exceptions.StorageExceptionNotRunning;
import net.jadoth.swizzling.types.Swizzle;

// (21.03.2016 TM)TODO: what is the difference between ~Manager and ~Controller here? Merge into Controller or comment.
public interface StorageManager extends StorageController
{
	public StorageRequestAcceptor createRequestAcceptor();

	public StorageTypeDictionary typeDictionary();

	// (20.05.2013)TODO: StorageManager#channelController() - not sure this belongs here
	public StorageChannelController channelController();

	public StorageConfiguration configuration();

	@Override
	public default StorageManager start()
	{
		return this.start(null, null); // null implies to use the standard / entityCache's evaluator
	}

	@Override
	public StorageManager start(
		StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		StorageTypeDictionary       oldTypes
	);

	@Override
	public boolean shutdown();

	public StorageObjectIdRangeEvaluator objectIdRangeEvaluator();

	/**
	 * Deletes all data in both memory and files and resets the storage into an empty pre-initialized state.
	 */
	public void truncateData();



	public final class Implementation implements StorageManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// composite members //
		private final StorageConfiguration                    configuration                ;
		private final StorageInitialDataFileNumberProvider    initialDataFileNumberProvider;
		private final StorageDataFileEvaluator                fileDissolver                ;
		private final StorageFileProvider                     fileProvider                 ;
		private final StorageFileReader.Provider              readerProvider               ;
		private final StorageFileWriter.Provider              writerProvider               ;
		private final StorageRequestAcceptor.Creator          communicatorCreator          ;
		private final StorageTaskBroker.Creator               taskBrokerCreator            ;
		private final StorageValidatorDataChunk.Provider      dataChunkValidatorProvider   ;
		private final StorageChannel.Creator                  channelCreator               ;
		private final StorageThreadProvider                   threadProvider               ;
		private final StorageEntityCacheEvaluator             entityCacheEvaluator         ;
		private final StorageRequestTaskCreator               requestTaskCreator           ;
		private final StorageTypeDictionary                   typeDictionary               ;
		private final StorageChannelController.Implementation channelController            ;
		private final StorageRootTypeIdProvider               rootTypeIdProvider           ;
		private final StorageExceptionHandler                 exceptionHandler             ;
		private final StorageWriteListener.Provider           writeListenerProvider        ;
		private final StorageHousekeepingController           housekeepingController       ;
		private final StorageTimestampProvider                timestampProvider            ;
		private final StorageObjectIdRangeEvaluator           objectIdRangeEvaluator       ;
		private final StorageValidRootIdCalculator.Provider   validRootIdCalculatorProvider;


		// state flags //
		private volatile boolean isRunning       ;
		private volatile boolean isStartingUp    ;
		// (15.06.2013)TODO: isAcceptingTasks: either use (methode) or delete or comment
		private volatile boolean isAcceptingTasks;
		private volatile boolean isShuttingDown  ;
		private volatile boolean isShutdown       = true ;
		private final    Object  stateLock        = new Object();

		// running state members //
		private volatile StorageTaskBroker    taskbroker   ;
		private final    ChannelKeeper[]      keepers      ;
		private          StorageWriteListener writeListener;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final StorageConfiguration                  storageConfiguration         ,
			final StorageInitialDataFileNumberProvider  initialDataFileNumberProvider,
			final StorageRequestAcceptor.Creator        requestAcceptorCreator       ,
			final StorageTaskBroker.Creator             taskBrokerCreator            ,
			final StorageValidatorDataChunk.Provider    dataChunkValidatorProvider   ,
			final StorageChannel.Creator                channelCreator               ,
			final StorageThreadProvider                 threadProvider               ,
			final StorageRequestTaskCreator             requestTaskCreator           ,
			final StorageTypeDictionary                 typeDictionary               ,
			final StorageRootTypeIdProvider             rootTypeIdProvider           ,
			final StorageTimestampProvider              timestampProvider            ,
			final StorageObjectIdRangeEvaluator         objectIdRangeEvaluator       ,
			final StorageFileReader.Provider            readerProvider               ,
			final StorageFileWriter.Provider            writerProvider               ,
			final StorageWriteListener.Provider         writeListenerProvider        ,
			final StorageValidRootIdCalculator.Provider validRootIdCalculatorProvider,
			final StorageExceptionHandler               exceptionHandler
		)
		{
			super();
			this.configuration                 = notNull(storageConfiguration)                ;
			this.initialDataFileNumberProvider = notNull(initialDataFileNumberProvider)       ;
			this.fileDissolver                 = storageConfiguration.fileEvaluator()         ;
			this.fileProvider                  = storageConfiguration.fileProvider()          ;
			this.entityCacheEvaluator          = storageConfiguration.entityCacheEvaluator()  ;
			this.housekeepingController        = storageConfiguration.housekeepingController();
			this.communicatorCreator           = notNull(requestAcceptorCreator)              ;
			this.taskBrokerCreator             = notNull(taskBrokerCreator)                   ;
			this.dataChunkValidatorProvider    = notNull(dataChunkValidatorProvider)          ;
			this.channelCreator                = notNull(channelCreator)                      ;
			this.threadProvider                = notNull(threadProvider)                      ;
			this.requestTaskCreator            = notNull(requestTaskCreator)                  ;
			this.typeDictionary                = notNull(typeDictionary)                      ;
			this.rootTypeIdProvider            = notNull(rootTypeIdProvider)                  ;
			this.timestampProvider             = notNull(timestampProvider)                   ;
			this.objectIdRangeEvaluator        = notNull(objectIdRangeEvaluator)              ;
			this.readerProvider                = notNull(readerProvider)                      ;
			this.writerProvider                = notNull(writerProvider)                      ;
			this.writeListenerProvider         = notNull(writeListenerProvider)               ;
			this.validRootIdCalculatorProvider = notNull(validRootIdCalculatorProvider)       ;
			this.exceptionHandler              = notNull(exceptionHandler)                    ;

			/* must not leave processing information implementation choice to outside context
			 * as this implementation relys on an immutable thread count.
			 */
			final StorageChannelCountProvider channelCountProvider = storageConfiguration.channelCountProvider();
			this.channelController = new StorageChannelController.Implementation(channelCountProvider);
			this.keepers           = new ChannelKeeper[channelCountProvider.get()];
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public final StorageConfiguration configuration()
		{
			return this.configuration;
		}

		@Override
		public final boolean isRunning()
		{
			return this.isRunning;
		}

		@Override
		public final boolean isAcceptingTasks()
		{
			return this.isAcceptingTasks;
		}

		@Override
		public final boolean isShutdown()
		{
			return this.isShutdown;
		}

		@Override
		public final boolean isStartingUp()
		{
			return this.isStartingUp;
		}

		@Override
		public final boolean isShuttingDown()
		{
			return this.isShuttingDown;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void ensureRunning()
		{
			if(this.isRunning)
			{
				return;
			}
			throw new StorageExceptionNotRunning();
		}

		private StorageIdRangeAnalysis startThreads(final StorageChannelTaskInitialize initializingTask)
			throws InterruptedException
		{
			// (07.07.2016 TM)TODO: StorageThreadStarter instead of hardcoded call
			synchronized(initializingTask)
			{
				for(final ChannelKeeper keeper : this.keepers)
				{
					keeper.thread.start();
				}
				initializingTask.waitOnCompletion();
			}
			return initializingTask.getIdRangeAnalysis();
		}



		// "Please do not disturb the Keepers" :-D
		static final class ChannelKeeper
		{
			final int            channelIndex;
			final StorageChannel processor   ;
			final Thread         thread      ;

			ChannelKeeper(final int channelIndex, final StorageChannel processor, final Thread thread)
			{
				super();
				this.channelIndex = channelIndex;
				this.processor    = processor   ;
				this.thread       = thread      ;
			}
		}

		private void createChannels()
		{
			/* (24.09.2014 TM)TODO: check channel directory consistency
			 * run analysis on provided storage base directory to see if there exist any channel folders
			 * that match, are less or are more than the channel count.
			 * Also check if some of the folders are empty.
			 * Give analysis result to configurable callback handler (exception by default).
			 */
			final StorageChannel[] channels = this.channelCreator.createChannels(
				this.channelCount()               ,
				this.initialDataFileNumberProvider,
				this.exceptionHandler             ,
				this.fileDissolver                ,
				this.fileProvider                 ,
				this.entityCacheEvaluator         ,
				this.typeDictionary               ,
				this.taskbroker                   ,
				this.channelController            ,
				this.housekeepingController       ,
				this.timestampProvider            ,
				this.readerProvider               ,
				this.writerProvider               ,
				this.createWriteListener()        ,
				this.validRootIdCalculatorProvider,
				this.rootTypeIdProvider.provideRootTypeId()
			);

			final ChannelKeeper[] keepers = this.keepers;
			for(int i = 0; i < channels.length; i++)
			{
				keepers[i] = new ChannelKeeper(i, channels[i], this.threadProvider.provideStorageThread(channels[i]));
			}
		}

		private StorageWriteListener createWriteListener()
		{
			return this.writeListener = this.writeListenerProvider.provideWriteListener(this.channelCount());
		}

		private int channelCount()
		{
			// once set, the channel count cannot be changed. Might be improved in the future.
			return this.keepers.length;
		}

		private void internalStartUp(
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
			final StorageTypeDictionary       oldTypes
		)
			throws InterruptedException
		{
			// thread safety and state consistency ensured prior to calling

			// create channels, setup task processing and start threads
			this.taskbroker = this.taskBrokerCreator.createTaskBroker(this, this.requestTaskCreator);
			final StorageChannelTaskInitialize task = this.taskbroker.issueChannelInitialization(
				this.channelController,
				entityInitializingCacheEvaluator,
				oldTypes
			);
			this.createChannels();

			final StorageIdRangeAnalysis idRangeAnalysis = this.startThreads(task);
			final Long                   maxOid          = idRangeAnalysis.highestIdsPerType().get(Swizzle.IdType.OID);

			// only ObjectId is relevant at this point
			this.objectIdRangeEvaluator.evaluateObjectIdRange(0, maxOid == null ? 0 : maxOid);

			this.writeListener.start();
		}

		private void internalShutdown() throws InterruptedException
		{
//			DEBUGStorage.println("shutting down ...");
			final StorageChannelTaskShutdown task = this.taskbroker.issueChannelShutdown(this.channelController);
			this.writeListener.stop();
			synchronized(task)
			{
				task.waitOnCompletion();
			}
//			DEBUGStorage.println("shutdown complete");
		}

		@Override
		public final void checkAcceptingTasks()
		{
			if(this.isAcceptingTasks)
			{
				return;
			}
			throw new StorageExceptionNotAcceptingTasks();
		}

		@Override
		public final StorageManager.Implementation start(
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
			final StorageTypeDictionary       oldTypes
		)
		{
			synchronized(this.stateLock)
			{
				if(!this.isShutdown)
				{
					throw new RuntimeException("already starting"); // (05.07.2014)EXCP: proper exception
				}
				this.isStartingUp = true;
				this.isShutdown = false;
				try
				{
					this.internalStartUp(entityInitializingCacheEvaluator, oldTypes);
					this.isRunning = true;
				}
				catch(final InterruptedException e)
				{
					throw new RuntimeException(e); // (15.06.2013)EXCP: proper exception
				}
				catch(final Throwable t)
				{
					this.isShutdown = true;
					throw t;
				}
				finally
				{
					this.isStartingUp = false;
				}
			}
			return this;
		}

		@Override
		public final boolean shutdown()
		{
			synchronized(this.stateLock)
			{
				try
				{
					this.internalShutdown();
					return true;
				}
				catch(final InterruptedException e)
				{
					// interruption while waiting for shutdown means don't shut down
					return false;
				}
			}
		}

		@Override
		public void truncateData()
		{
			try
			{
				final StorageChannelTaskTruncateData task = this.taskbroker.issueTruncateData(this.channelController);
				synchronized(task)
				{
					task.waitOnCompletion();
				}
			}
			catch(final InterruptedException e)
			{
				// interrupted truncation, abort
				return;
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.typeDictionary;
		}

		@Override
		public StorageChannelController channelController()
		{
			return this.channelController;
		}

		@Override
		public StorageObjectIdRangeEvaluator objectIdRangeEvaluator()
		{
			return this.objectIdRangeEvaluator;
		}

		@Override
		public final StorageRequestAcceptor createRequestAcceptor()
		{
			this.ensureRunning();

			return this.communicatorCreator.createCommunicator(
				this.dataChunkValidatorProvider.provideDataChunkValidator(),
				this.taskbroker
			);
		}

	}

}
