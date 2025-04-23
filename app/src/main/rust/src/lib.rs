use jni::objects::{JClass, JString};
use jni::JNIEnv;
//<RustJNI>
// primitive imports
use jni::sys::{jstring};

//</RustJNI>

mod data;
mod model;
mod training;
use crate::{model::ModelConfig, training::TrainingConfig};
use burn::{
    backend::{Autodiff, Wgpu},
    optim::AdamConfig,
};

use std::fs::File;
use std::io::{BufRead, BufReader};

#[no_mangle]
pub extern "C" fn Java_com_example_rust3_MainActivity_startTraining(
    mut env: JNIEnv,
    _class: JClass,
    artifactPath: jstring, locationString: jstring
) -> jstring {

    
    let jsstring_path: JString<'_> = unsafe { JString::from_raw(artifactPath) };
    let artifact_path: String = env
        .get_string(&jsstring_path)
        .expect("Couldn't get artifact path from Java")
        .to_string_lossy()
        .into_owned();
    
    let jsstring_location: JString<'_> = unsafe { JString::from_raw(locationString) };
    let coordinates: String = env
        .get_string(&jsstring_location)
        .expect("Couldn't get artifact path from Java")
        .to_string_lossy()
        .into_owned();


    // Model config
    let hidden_size: usize = 256;
    // dropout should remain close to 0.5
    let dropout: f64 = 0.5;

    // Training config
    let num_epochs: usize = 20;
    let batch_size: usize = 64;
    let num_workers: usize = 4;
    // Seed ensures reproducibility
    let seed: u64 = 42;
    let learning_rate: f64 = 1.0e-4;
    // Optimizer should not by changed, but can be if needed
    let optimizer_config = AdamConfig::new();

    type MyBackend = Wgpu<f32, i32>;

    let device = Default::default();

    let model_config = ModelConfig {
        hidden_size,
        dropout,
    };

    model_config.init::<MyBackend>(&device);

    //let model_text = format!("{:#?}", model);

    type MyAutodiffBackend = Autodiff<MyBackend>;
    let device = burn::backend::wgpu::WgpuDevice::default();

    let training_config = TrainingConfig {
        model: model_config,
        optimizer: optimizer_config,
        num_epochs,
        batch_size,
        num_workers,
        seed,
        learning_rate,
    };

    
    //let unformated_data:String = String::from("1745405818897,(57.0121349, 9.9908265),(57.0121448,9.9907374),(57.0121381, 9.9907434),");
    
    crate::training::train::<MyAutodiffBackend>(&artifact_path, training_config, device.clone(), coordinates);

    let path_to_something = format!("{}/train/epoch-1/Loss.log", artifact_path);
    let path_to_something2 = format!("{}/train/epoch-20/Loss.log", artifact_path);

    let kage_text = match File::open(&path_to_something) {
        Ok(file) => {
            let reader = BufReader::new(file);
            match reader.lines().next() {
                Some(Ok(first_line)) => {
                    // If the first line is successfully read, save it to kage_text
                    format!("1: {}", first_line)
                }
                Some(Err(e)) => {
                    // Handle the error if reading the first line fails
                    format!("Error reading the first line: {}", e)
                }
                None => {
                    // Handle the case where the file is empty or there is an issue
                    "File is empty or error occurred while reading.".to_string()
                }
            }
        }
        Err(e) => {
            // Handle file open error
            format!("Failed to open file {}: {}", path_to_something, e)
        }
    };

    let kage_text2 = match File::open(&path_to_something2) {
        Ok(file) => {
            let reader = BufReader::new(file);
            match reader.lines().next() {
                Some(Ok(first_line)) => {
                    // If the first line is successfully read, save it to kage_text
                    format!("20: {}", first_line)
                }
                Some(Err(e)) => {
                    // Handle the error if reading the first line fails
                    format!("Error reading the first line: {}", e)
                }
                None => {
                    // Handle the case where the file is empty or there is an issue
                    "File is empty or error occurred while reading.".to_string()
                }
            }
        }
        Err(e) => {
            // Handle file open error
            format!("Failed to open file {}: {}", path_to_something, e)
        }
    };
    let noget = format!("{:#?}, {:#?}", kage_text, kage_text2);

     
    
    //let noget = format!("{:#?}, {:#?}", coordinates, unformated_data);
      
    env.new_string(noget)
        .expect("Couldn't create Java string!")
        .into_raw()
}                        
        
        
                        
        
        