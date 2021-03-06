package io.hydrosphere.serving.kafka.it.infrostructure

import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import io.hydrosphere.serving.kafka.config.{ApplicationConfig, Configuration, KafkaConfiguration, SidecarConfig}
import io.hydrosphere.serving.kafka.grpc.PredictionGrpcApi
import io.hydrosphere.serving.kafka.kafka_messages.KafkaServingMessage
import io.hydrosphere.serving.kafka.mappers.{KafkaServingMessageSerde, KafkaServingMessageSerializer}
import io.hydrosphere.serving.kafka.predict.{PredictService, PredictServiceImpl, XDSApplicationUpdateService}
import io.hydrosphere.serving.kafka.stream.{KafkaStreamer, Producer}
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder

object TestInject {

  implicit val config = Configuration(
    ApplicationConfig("hydro-serving-kafka", 56789),
    SidecarConfig("localhost", 56788, 56688, 56687),
    KafkaConfiguration("localhost", 9092)
  )

  implicit val modelChanel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 56787).usePlaintext(true).build
  val appChanel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 56786).usePlaintext(true).build

  implicit val predictService: PredictService = new PredictServiceImpl

  implicit val applicationUpdater = new XDSApplicationUpdateService()(appChanel)

  implicit lazy val streamsBuilder = new StreamsBuilder()

  implicit val streamer = new KafkaStreamer[Array[Byte], KafkaServingMessage](Serdes.ByteArray().getClass, classOf[KafkaServingMessageSerde])

  implicit lazy val kafkaProducer = Producer[Array[Byte], KafkaServingMessage](
    config,
    Serdes.ByteArray().serializer().getClass,
    classOf[KafkaServingMessageSerializer])

  implicit val predictionApi = new PredictionGrpcApi


}
