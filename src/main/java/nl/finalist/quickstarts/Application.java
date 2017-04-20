/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package nl.finalist.quickstarts;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class Application extends RouteBuilder {

    @Override
    public void configure() {
    	
        restConfiguration()
            .contextPath("/camel-rest-sql").apiContextPath("/api-doc")
                .apiProperty("api.title", "Camel REST API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiContextRouteId("doc-api")
            .component("jetty")
            .port(8080)
            .bindingMode(RestBindingMode.json);

        rest("/books").description("Books REST service")
            .get("/").description("The list of all the books")
                .route().routeId("books-api")
                .to("sql:select distinct description from orders?" +
                    "dataSource=dataSource&" +
                    "outputClass=nl.finalist.quickstarts.Book")
                .endRest()
            .get("order/{id}").description("Details of an order by id")
                .route().routeId("order-api")
                .to("sql:select * from orders where id = :#${header.id}?" +
                    "dataSource=dataSource&outputType=SelectOne&" +
                    "outputClass=nl.finalist.quickstarts.Order");
        // A first route generates some orders and queue them in DB
        from("timer:new-order?delay=1s&period={{quickstart.generateOrderPeriod:2s}}")
            .routeId("generate-order")
            .bean("orderService", "generateOrder")
            .to("sql:insert into orders (id, item, amount, description, processed) values " +
                "(:#${body.id} , :#${body.item}, :#${body.amount}, :#${body.description}, false)?" +
                "dataSource=dataSource")
            .log("Inserted new order ${body.id}");

        // A second route polls the DB for new orders and processes them
        from("sql:select * from orders where processed = false?" +
            "consumer.onConsume=update orders set processed = true where id = :#id&" +
            "consumer.delay={{quickstart.processOrderPeriod:5s}}&" +
            "dataSource=dataSource")
            .routeId("process-order")
            .bean("orderService", "rowToOrder")
            .log("Processed order #id ${body.id} with ${body.amount} copies of the «${body.description}» book");
    }
}