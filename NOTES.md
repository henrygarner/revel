A revel plot has a 3 stage lifecycle:

* A configuration stage
  This is where axes are specified, layers are added, and faceting can be requested. During this part of the lifecycle the revel representation can be best considered as a configuration map, with helper functions available to alter the configuration in helpful ways. The configuration may specify data, either globally or for each layer, but the data is transparent to the rest of the configuration.
  
* A scale domain stage
  This is after data is supplied to the plot, and configuration is read in order how to process the data in order to create scales and axes with a specific domain. Scales are configured to have an appropriate domain which takes into account any configuration, custom data, faceting in operation.
  There are several sub-steps:
  - Give each layer the opportunity to calibrate on the entire dataset
  - And, if faceting:
  - Create faceted versions of each layer
  - Give each facet scale the opportunity to calibrate on its own subset 
  - Give each layer the opportunity to reach consensus on the domain being used.
    - Is this only a problem for faceted histograms in scale-lock mode? All scales should be the same but we can only know this from taking the extremal values across all facets. Should bins be automatically determined the same way, i.e. determine optimal bins independently and _then_ aim to reach a compromise value? If so, this must be done before doing the same for the value since the values will obviously change in response. In this way value is dependent on the bin specification: how should this be encoded? It's clearly not the case that y is dependent on x because it is easy to imagine rotating the histogram 90 degrees.
  
Questions:
  - How to represent a grid of facets
  - How precisely to reach consensus of scale domains
  - How to represent categorical or other domains (same as dataset?)
  
* A scale range stage
  This stage is handled by a particular renderer. Scale domains are mapped to ranges which make sense in the context of a particular renderer.
  
